package kr.spot.application.event;

import kr.spot.IdGenerator;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationType;
import kr.spot.domain.vo.Content;
import kr.spot.domain.vo.NotificationTarget;
import kr.spot.event.StudyApplicationProcessedEvent;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyApplicationNotificationListener {

  private final IdGenerator idGenerator;
  private final NotificationRepository notificationRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handle(StudyApplicationProcessedEvent event) {
    try {
      Notification notification = createNotification(event);
      notificationRepository.save(notification);
      log.info("Notification saved: studyId={}, applicantId={}, decision={}",
          event.studyId(), event.applicantId(), event.decision());
    } catch (Exception e) {
      log.error("Failed to save notification: {}", event, e);
    }
  }

  private Notification createNotification(StudyApplicationProcessedEvent event) {
    Content content = Content.of(
        buildTitle(event),
        buildMessage(event),
        event.studyThumbnailUrl()
    );

    NotificationTarget target = NotificationTarget.of(
        event.applicantId(),
        event.studyId(),
        NotificationType.STUDY_APPLICATION_RESULT,
        null
    );

    return Notification.of(idGenerator.nextId(), content, target);
  }

  private String buildTitle(StudyApplicationProcessedEvent event) {
    return event.studyName() + (event.isApproved() ? " 신청이 수락되었어요!" : " 신청이 거절되었어요.");
  }

  private String buildMessage(StudyApplicationProcessedEvent event) {
    if (event.isApproved()) {
      return String.format("'%s' 스터디 가입이 승인되었습니다.", event.studyName());
    }
    return String.format("'%s' 스터디 가입이 거절되었습니다.", event.studyName());
  }
}
