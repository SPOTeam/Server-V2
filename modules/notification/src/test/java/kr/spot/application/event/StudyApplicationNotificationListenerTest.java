package kr.spot.application.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationStatus;
import kr.spot.domain.enums.NotificationType;
import kr.spot.event.StudyApplicationProcessedEvent;
import kr.spot.infrastructure.jpa.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    assertThat(notification.getTitle()).isEqualTo("자바 스터디");
    assertThat(notification.getBody()).contains("승인");
    assertThat(notification.getMemberId()).isEqualTo(2L);
    assertThat(notification.getReferenceId()).isEqualTo(1L);
    assertThat(notification.getReferenceType()).isEqualTo("STUDY");
    assertThat(notification.getType()).isEqualTo(NotificationType.STUDY_APPLICATION_APPROVED);
    assertThat(notification.getDispatchStatus()).isEqualTo(NotificationStatus.PENDING);
  }

  @Test
  @Disabled
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
    assertThat(notification.getTitle()).isEqualTo("자바 스터디");
    assertThat(notification.getBody()).contains("거절");
    assertThat(notification.getType()).isEqualTo(NotificationType.STUDY_APPLICATION_REJECTED);
  }
}
