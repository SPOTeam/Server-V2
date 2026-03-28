package kr.spot.application.dispatch;

import java.time.Duration;
import java.time.LocalDateTime;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationStatus;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

  private final PushNotificationClient pushClient;
  private final NotificationRepository notificationRepository;

  @Async("notificationExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendAsync(long notificationId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElse(null);

    if (notification == null) {
      log.warn("Notification not found: id={}", notificationId);
      return;
    }

    // 발송 전 상태 재확인 (다른 서버가 이미 처리했을 수 있음)
    if (notification.getDispatchStatus() != NotificationStatus.PROCESSING) {
      log.info("Notification {} already processed, status={}",
          notificationId, notification.getDispatchStatus());
      return;
    }

    try {
      pushClient.send(notification);

      // 성공
      notification.markAsSent(LocalDateTime.now());
      notificationRepository.save(notification);

      log.debug("Notification sent successfully: id={}, memberId={}",
          notificationId, notification.getMemberId());

    } catch (Exception e) {
      handleFailure(notification, e);
    }
  }

  private void handleFailure(Notification notification, Exception e) {
    log.warn("Failed to send notification: id={}, error={}",
        notification.getId(), e.getMessage());

    if (notification.canRetry()) {
      // 재시도 예약 (Exponential Backoff)
      Duration backoff = calculateBackoff(notification.getRetryCount() + 1);
      notification.scheduleRetry(LocalDateTime.now().plus(backoff));

      log.info("Notification {} scheduled for retry #{} at {}",
          notification.getId(),
          notification.getRetryCount(),
          notification.getNextRetryAt());
    } else {
      // 최종 실패
      notification.markAsFailed(truncateError(e.getMessage()));

      log.error("Notification {} permanently failed after {} retries",
          notification.getId(),
          notification.getRetryCount());
    }

    notificationRepository.save(notification);
  }

  private Duration calculateBackoff(int retryCount) {
    // 1분, 5분, 30분
    return switch (retryCount) {
      case 1 -> Duration.ofMinutes(1);
      case 2 -> Duration.ofMinutes(5);
      default -> Duration.ofMinutes(30);
    };
  }

  private String truncateError(String message) {
    if (message == null) {
      return "Unknown error";
    }
    return message.length() > 500 ? message.substring(0, 500) : message;
  }
}
