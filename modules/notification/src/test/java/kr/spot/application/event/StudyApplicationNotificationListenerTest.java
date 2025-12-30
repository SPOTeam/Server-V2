package kr.spot.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationType;
import kr.spot.event.StudyApplicationProcessedEvent;
import kr.spot.infrastructure.jpa.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudyApplicationNotificationListenerTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  NotificationRepository notificationRepository;

  @Captor
  ArgumentCaptor<Notification> notificationCaptor;

  StudyApplicationNotificationListener listener;

  @BeforeEach
  void setUp() {
    listener = new StudyApplicationNotificationListener(idGenerator, notificationRepository);
  }

  @Test
  @DisplayName("승인 이벤트 수신 시 알림이 저장된다")
  void should_save_notification_when_approved() {
    // given
    Long notificationId = 100L;
    StudyApplicationProcessedEvent event = StudyApplicationProcessedEvent.of(
        1L, 2L, 3L, "APPROVE", "자바 스터디", "http://image.url/java.png");

    when(idGenerator.nextId()).thenReturn(notificationId);
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    listener.handle(event);

    // then
    verify(notificationRepository).save(notificationCaptor.capture());

    Notification notification = notificationCaptor.getValue();
    assertThat(notification.getId()).isEqualTo(notificationId);
    assertThat(notification.getContent().getTitle()).isEqualTo("자바 스터디 신청이 수락되었어요!");
    assertThat(notification.getContent().getContent()).contains("자바 스터디");
    assertThat(notification.getContent().getContent()).contains("승인");
    assertThat(notification.getTarget().getTargetMemberId()).isEqualTo(2L);
    assertThat(notification.getTarget().getLinkStudyId()).isEqualTo(1L);
    assertThat(notification.getTarget().getNotificationType())
        .isEqualTo(NotificationType.STUDY_APPLICATION_RESULT);
  }

  @Test
  @DisplayName("거절 이벤트 수신 시 알림이 저장된다")
  void should_save_notification_when_rejected() {
    // given
    Long notificationId = 100L;
    StudyApplicationProcessedEvent event = StudyApplicationProcessedEvent.of(
        1L, 2L, 3L, "REJECT", "자바 스터디", "http://image.url");

    when(idGenerator.nextId()).thenReturn(notificationId);
    when(notificationRepository.save(any(Notification.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // when
    listener.handle(event);

    // then
    verify(notificationRepository).save(notificationCaptor.capture());

    Notification notification = notificationCaptor.getValue();
    assertThat(notification.getContent().getTitle()).isEqualTo("자바 스터디 신청이 거절되었어요.");
    assertThat(notification.getContent().getContent()).contains("거절");
  }
}
