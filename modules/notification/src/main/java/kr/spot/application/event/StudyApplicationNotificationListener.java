package kr.spot.application.event;

import java.time.LocalDateTime;
import kr.spot.IdGenerator;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationType;
import kr.spot.event.StudyApplicationProcessedEvent;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 스터디 신청 처리 결과 알림을 생성합니다.
 * study 모듈에서 발행하는 StudyApplicationProcessedEvent를 처리합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StudyApplicationNotificationListener {

    private final IdGenerator idGenerator;
    private final NotificationRepository notificationRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handle(StudyApplicationProcessedEvent event) {
        try {
            Notification notification = createNotification(event);
            notificationRepository.save(notification);
            log.info("Notification saved: studyId={}, applicantId={}, decision={}",
                event.studyId(), event.applicantId(), event.decision());
        } catch (DataIntegrityViolationException e) {
            // dedupe_key 중복 - 이미 동일한 알림이 존재함
            log.debug("Duplicate notification ignored for studyId={}, applicantId={}",
                event.studyId(), event.applicantId());
        } catch (Exception e) {
            log.error("Failed to save notification: {}", event, e);
        }
    }

    private Notification createNotification(StudyApplicationProcessedEvent event) {
        NotificationType type = event.isApproved()
            ? NotificationType.STUDY_APPLICATION_APPROVED
            : NotificationType.STUDY_APPLICATION_REJECTED;

        String title = event.studyName();
        String body = event.isApproved()
            ? "스터디 가입이 승인되었습니다! 지금 바로 참여해보세요."
            : "스터디 가입이 거절되었습니다.";

        String dedupeKey = String.format("%s:STUDY:%d:%d",
            type.name(), event.studyId(), event.applicantId());

        return Notification.create(
            idGenerator.nextId(),
            event.applicantId(),
            type,
            title,
            body,
            event.studyThumbnailUrl(),
            "STUDY",
            event.studyId(),
            LocalDateTime.now(),
            dedupeKey
        );
    }
}
