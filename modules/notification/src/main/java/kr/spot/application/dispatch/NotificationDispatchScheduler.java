package kr.spot.application.dispatch;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.List;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationStatus;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationDispatchScheduler {

  private static final int BATCH_SIZE = 100;

  private final NotificationRepository notificationRepository;
  private final NotificationSender notificationSender;
  private final String serverId = resolveServerId();

  private static String resolveServerId() {
    try {
      String hostname = InetAddress.getLocalHost().getHostName();
      return hostname.length() > 50 ? hostname.substring(0, 50) : hostname;
    } catch (UnknownHostException e) {
      return "server-" + System.currentTimeMillis();
    }
  }

  /**
   * 발송 대상 알림을 선점하고 비동기로 발송합니다. 10초마다 실행됩니다.
   */
  @Scheduled(fixedDelay = 10_000)
  @Transactional
  public void dispatch() {
    LocalDateTime now = LocalDateTime.now();

    // 1. 발송 대상 선점 (FOR UPDATE SKIP LOCKED)
    int pickedCount = notificationRepository.pickPendingNotifications(
        serverId, now, BATCH_SIZE
    );

    if (pickedCount == 0) {
      return;
    }

    log.info("Picked {} notifications for dispatch", pickedCount);

    // 2. 선점한 알림 조회
    List<Notification> notifications = notificationRepository
        .findByPickedByAndDispatchStatus(serverId, NotificationStatus.PROCESSING);

    // 3. 비동기로 발송 처리
    for (Notification notification : notifications) {
      notificationSender.sendAsync(notification.getId());
    }
  }
}
