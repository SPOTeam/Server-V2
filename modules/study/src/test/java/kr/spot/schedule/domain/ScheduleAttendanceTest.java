package kr.spot.schedule.domain;

import static kr.spot.schedule.common.AttendanceFixture.QR_CODE_URL;
import static kr.spot.schedule.common.AttendanceFixture.SCHEDULE_ID;
import static kr.spot.schedule.common.AttendanceFixture.STUDY_ID;
import static kr.spot.schedule.common.AttendanceFixture.futureSchedule;
import static kr.spot.schedule.common.AttendanceFixture.ongoingSchedule;
import static kr.spot.schedule.common.AttendanceFixture.ongoingScheduleWithAttendance;
import static kr.spot.schedule.common.AttendanceFixture.pastSchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScheduleAttendanceTest {

  @Nested
  @DisplayName("출석체크 시작")
  class StartAttendance {

    @Test
    @DisplayName("진행 중인 일정에서 출석체크를 시작할 수 있다")
    void should_startAttendance_when_scheduleIsOngoing() {
      // given
      Schedule schedule = ongoingSchedule();

      // when
      schedule.startAttendance(STUDY_ID);

      // then
      assertThat(schedule.isAttendanceActive()).isTrue();
    }

    @Test
    @DisplayName("다른 스터디의 일정에서는 출석체크를 시작할 수 없다")
    void should_throwException_when_differentStudyId() {
      // given
      Schedule schedule = ongoingSchedule();
      long wrongStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> schedule.startAttendance(wrongStudyId))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._SCHEDULE_ACCESS_DENIED);
    }

    @Test
    @DisplayName("일정 시간 외에는 출석체크를 시작할 수 없다")
    void should_throwException_when_scheduleNotOngoing() {
      // given
      Schedule schedule = pastSchedule();

      // when & then
      assertThatThrownBy(() -> schedule.startAttendance(STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }

    @Test
    @DisplayName("미래 일정에서는 출석체크를 시작할 수 없다")
    void should_throwException_when_scheduleInFuture() {
      // given
      Schedule schedule = futureSchedule();

      // when & then
      assertThatThrownBy(() -> schedule.startAttendance(STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }

    @Test
    @DisplayName("이미 출석체크가 진행 중이면 예외를 던진다")
    void should_throwException_when_attendanceAlreadyActive() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      // when & then
      assertThatThrownBy(() -> schedule.startAttendance(STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._SCHEDULE_QR_CODE_ALREADY_ASSIGNED);
    }
  }

  @Nested
  @DisplayName("출석체크 종료")
  class StopAttendance {

    @Test
    @DisplayName("출석체크를 종료하면 비활성 상태가 된다")
    void should_deactivateAttendance_when_stopped() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      // when
      schedule.stopAttendance(STUDY_ID);

      // then
      assertThat(schedule.isAttendanceActive()).isFalse();
    }

    @Test
    @DisplayName("출석체크 종료 시 QR 코드 URL이 초기화된다")
    void should_clearQrCodeUrl_when_stopped() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();
      schedule.updateQrCodeImageUrl(QR_CODE_URL);

      // when
      schedule.stopAttendance(STUDY_ID);

      // then
      assertThat(schedule.getAttendanceQrCodeImageUrl()).isNull();
    }

    @Test
    @DisplayName("다른 스터디의 일정은 출석체크를 종료할 수 없다")
    void should_throwException_when_differentStudyId() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();
      long wrongStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> schedule.stopAttendance(wrongStudyId))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._SCHEDULE_ACCESS_DENIED);
    }
  }

  @Nested
  @DisplayName("출석체크 가능 여부 검증")
  class ValidateAttendanceCheckable {

    @Test
    @DisplayName("출석체크가 활성화되고 진행 중인 일정이면 검증에 성공한다")
    void should_passValidation_when_attendanceActiveAndOngoing() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      // when & then (no exception thrown)
      schedule.validateAttendanceCheckable();
    }

    @Test
    @DisplayName("출석체크가 비활성화되어 있으면 검증에 실패한다")
    void should_failValidation_when_attendanceNotActive() {
      // given
      Schedule schedule = ongoingSchedule();

      // when & then
      assertThatThrownBy(schedule::validateAttendanceCheckable)
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_STARTED);
    }
  }

  @Nested
  @DisplayName("QR 코드 URL 업데이트")
  class UpdateQrCodeImageUrl {

    @Test
    @DisplayName("QR 코드 URL을 업데이트할 수 있다")
    void should_updateQrCodeUrl() {
      // given
      Schedule schedule = ongoingSchedule();

      // when
      schedule.updateQrCodeImageUrl(QR_CODE_URL);

      // then
      assertThat(schedule.getAttendanceQrCodeImageUrl()).isEqualTo(QR_CODE_URL);
    }
  }

  @Nested
  @DisplayName("일정 진행 중 확인")
  class IsOngoing {

    @Test
    @DisplayName("현재 시간이 일정 시간 내에 있으면 true를 반환한다")
    void should_returnTrue_when_currentTimeWithinSchedule() {
      // given
      Schedule schedule = ongoingSchedule();

      // when & then
      assertThat(schedule.isOngoing(java.time.LocalDateTime.now())).isTrue();
    }

    @Test
    @DisplayName("일정 시간이 지났으면 false를 반환한다")
    void should_returnFalse_when_scheduleEnded() {
      // given
      Schedule schedule = pastSchedule();

      // when & then
      assertThat(schedule.isOngoing(java.time.LocalDateTime.now())).isFalse();
    }

    @Test
    @DisplayName("일정 시간 전이면 false를 반환한다")
    void should_returnFalse_when_scheduleNotStarted() {
      // given
      Schedule schedule = futureSchedule();

      // when & then
      assertThat(schedule.isOngoing(java.time.LocalDateTime.now())).isFalse();
    }
  }
}
