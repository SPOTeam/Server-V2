package kr.spot.application.event;

import static org.mockito.Mockito.verify;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.infrastructure.jpa.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupNotificationOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  NotificationRepository notificationRepository;

  CleanupNotificationOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupNotificationOnMemberWithdrawn(notificationRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 알림을 삭제한다")
  void should_deleteNotifications_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    verify(notificationRepository).deleteByTargetTargetMemberId(MEMBER_ID);
  }
}
