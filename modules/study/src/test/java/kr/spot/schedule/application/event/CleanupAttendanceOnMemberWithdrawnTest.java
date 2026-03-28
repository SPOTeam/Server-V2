package kr.spot.schedule.application.event;

import static org.mockito.Mockito.verify;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupAttendanceOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  AttendanceRepository attendanceRepository;

  CleanupAttendanceOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupAttendanceOnMemberWithdrawn(attendanceRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 출석 기록을 삭제한다")
  void should_deleteAttendance_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    verify(attendanceRepository).deleteByMemberInfoMemberId(MEMBER_ID);
  }
}
