package kr.spot.schedule.domain;

import static kr.spot.schedule.common.AttendanceFixture.ATTENDANCE_ID;
import static kr.spot.schedule.common.AttendanceFixture.SCHEDULE_ID;
import static kr.spot.schedule.common.AttendanceFixture.memberInfo;
import static org.assertj.core.api.Assertions.assertThat;

import kr.spot.schedule.domain.enums.AttendanceStatus;
import kr.spot.schedule.domain.vo.MemberInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AttendanceTest {

  @Nested
  @DisplayName("출석 기록 생성")
  class CreateAttendance {

    @Test
    @DisplayName("출석 기록 생성 시 초기 상태는 UNDECIDED이다")
    void should_createWithUndecidedStatus() {
      // given
      MemberInfo member = memberInfo();

      // when
      Attendance attendance = Attendance.of(ATTENDANCE_ID, SCHEDULE_ID, member);

      // then
      assertThat(attendance.getId()).isEqualTo(ATTENDANCE_ID);
      assertThat(attendance.getScheduleId()).isEqualTo(SCHEDULE_ID);
      assertThat(attendance.getMemberInfo()).isEqualTo(member);
      assertThat(attendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.UNDECIDED);
      assertThat(attendance.getAttendedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("출석 상태 변경")
  class MarkAttendance {

    @Test
    @DisplayName("출석으로 상태를 변경할 수 있다")
    void should_markAsPresent() {
      // given
      Attendance attendance = Attendance.of(ATTENDANCE_ID, SCHEDULE_ID, memberInfo());

      // when
      attendance.markAttendance(AttendanceStatus.PRESENT);

      // then
      assertThat(attendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("결석으로 상태를 변경할 수 있다")
    void should_markAsAbsent() {
      // given
      Attendance attendance = Attendance.of(ATTENDANCE_ID, SCHEDULE_ID, memberInfo());

      // when
      attendance.markAttendance(AttendanceStatus.ABSENT);

      // then
      assertThat(attendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT);
    }

    @Test
    @DisplayName("출석 상태 변경 시 출석 시간이 갱신된다")
    void should_updateAttendedAt_when_markAttendance() {
      // given
      Attendance attendance = Attendance.of(ATTENDANCE_ID, SCHEDULE_ID, memberInfo());
      var originalTime = attendance.getAttendedAt();

      // when
      attendance.markAttendance(AttendanceStatus.PRESENT);

      // then
      assertThat(attendance.getAttendedAt()).isAfterOrEqualTo(originalTime);
    }
  }
}
