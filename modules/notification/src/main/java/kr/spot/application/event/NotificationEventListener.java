package kr.spot.application.event;

import java.util.List;
import java.util.Map;
import kr.spot.IdGenerator;
import kr.spot.application.recipient.RecipientResolver;
import kr.spot.application.template.NotificationContent;
import kr.spot.application.template.NotificationTemplateRenderer;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationType;
import kr.spot.event.NotificationRequestedEvent;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

  private final IdGenerator idGenerator;
  private final NotificationRepository notificationRepository;
  private final NotificationTemplateRenderer templateRenderer;
  private final RecipientResolver recipientResolver;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(NotificationRequestedEvent event) {
    try {
      NotificationType type = parseNotificationType(event.type());
      Map<String, Object> payload = event.payload();

      // 1. 수신자 목록 조회
      List<Long> recipientIds = recipientResolver.resolve(type, payload);
      if (recipientIds.isEmpty()) {
        log.warn("No recipients found for notification type: {}", type);
        return;
      }

      // 2. 템플릿 렌더링
      NotificationContent content = templateRenderer.render(type, payload);

      // 3. 알림 레코드 생성 및 저장
      List<Notification> notifications = recipientIds.stream()
          .map(memberId -> createNotification(
              memberId, type, content, event
          ))
          .toList();

      saveNotifications(notifications);

      log.info("Created {} notifications for type: {}", notifications.size(), type);
    } catch (GeneralException e) {
      log.error("Failed to create notifications: errorCode={}, message={}",
          e.getStatus().getCode(), e.getStatus().getMessage());
      throw e;
    } catch (Exception e) {
      log.error("Failed to create notifications for event: {}", event, e);
    }
  }

  private NotificationType parseNotificationType(String type) {
    try {
      return NotificationType.valueOf(type);
    } catch (IllegalArgumentException e) {
      throw new GeneralException(ErrorStatus._INVALID_NOTIFICATION_TYPE);
    }
  }

  private Notification createNotification(
      long memberId,
      NotificationType type,
      NotificationContent content,
      NotificationRequestedEvent event
  ) {
    String dedupeKey = generateDedupeKey(type, event.referenceId(), memberId);

    return Notification.create(
        idGenerator.nextId(),
        memberId,
        type,
        content.title(),
        content.body(),
        event.imageUrl(),
        type.getReferenceType(),
        event.referenceId(),
        event.scheduledAt(),
        dedupeKey
    );
  }

  private String generateDedupeKey(NotificationType type, Long referenceId, long memberId) {
    if (referenceId == null) {
      return null; // 중복 방지 키가 없는 경우 (일반 알림)
    }
    return String.format("%s:%s:%d:%d",
        type.name(),
        type.getReferenceType(),
        referenceId,
        memberId
    );
  }

  private void saveNotifications(List<Notification> notifications) {
    for (Notification notification : notifications) {
      try {
        notificationRepository.save(notification);
      } catch (DataIntegrityViolationException e) {
        // dedupe_key 중복 - 이미 동일한 알림이 존재함 (멱등성 보장)
        log.debug("Duplicate notification ignored: dedupeKey={}",
            notification.getDedupeKey());
      }
    }
  }
}
