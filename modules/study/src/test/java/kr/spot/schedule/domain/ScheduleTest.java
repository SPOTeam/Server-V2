package kr.spot.schedule.domain;

import static kr.spot.schedule.common.ScheduleFixture.CREATOR_ID;
import static kr.spot.schedule.common.ScheduleFixture.END_AT;
import static kr.spot.schedule.common.ScheduleFixture.ID;
import static kr.spot.schedule.common.ScheduleFixture.LOCATION_MEMO;
import static kr.spot.schedule.common.ScheduleFixture.START_AT;
import static kr.spot.schedule.common.ScheduleFixture.STUDY_ID;
import static kr.spot.schedule.common.ScheduleFixture.TITLE;
import static kr.spot.schedule.common.ScheduleFixture.schedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScheduleTest {

  @Nested
  @DisplayName("일정 생성 (of)")
  class CreateSchedule {

    @Test
    @DisplayName("일정 객체를 정상적으로 생성할 수 있다")
    void should_create_schedule_successfully() {
      // when
      Schedule schedule = Schedule.of(ID, STUDY_ID, CREATOR_ID, TITLE, LOCATION_MEMO, START_AT, END_AT);

      // then
      assertThat(schedule).isNotNull();
      assertThat(schedule.getId()).isEqualTo(ID);
      assertThat(schedule.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(schedule.getCreatorId()).isEqualTo(CREATOR_ID);
      assertThat(schedule.getTitle()).isEqualTo(TITLE);
      assertThat(schedule.getLocationMemo()).isEqualTo(LOCATION_MEMO);
      assertThat(schedule.getStartAt()).isEqualTo(START_AT);
      assertThat(schedule.getEndAt()).isEqualTo(END_AT);
    }

    @Test
    @DisplayName("위치 정보 없이 일정 객체를 생성할 수 있다")
    void should_create_schedule_without_location() {
      // when
      Schedule schedule = Schedule.of(ID, STUDY_ID, CREATOR_ID, TITLE, null, START_AT, END_AT);

      // then
      assertThat(schedule).isNotNull();
      assertThat(schedule.getLocationMemo()).isNull();
    }

    @Test
    @DisplayName("시작/종료 시간 없이 일정 객체를 생성할 수 있다")
    void should_create_schedule_without_time() {
      // when
      Schedule schedule = Schedule.of(ID, STUDY_ID, CREATOR_ID, TITLE, LOCATION_MEMO, null, null);

      // then
      assertThat(schedule).isNotNull();
      assertThat(schedule.getStartAt()).isNull();
      assertThat(schedule.getEndAt()).isNull();
    }
  }

  @Nested
  @DisplayName("일정 삭제 (delete)")
  class DeleteSchedule {

    @Test
    @DisplayName("해당 스터디의 일정을 삭제하면 예외가 발생하지 않는다")
    void should_delete_schedule_successfully() {
      // given
      Schedule schedule = schedule();

      // when & then (예외가 발생하지 않으면 성공)
      schedule.delete(STUDY_ID);
    }

    @Test
    @DisplayName("다른 스터디의 일정을 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_with_different_study_id() {
      // given
      Schedule schedule = schedule();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> schedule.delete(otherStudyId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._SCHEDULE_ACCESS_DENIED);
    }
  }

  @Nested
  @DisplayName("진행 중 여부 확인 (isOngoing)")
  class IsOngoing {

    @Test
    @DisplayName("현재 시간이 일정 시간 범위 내에 있으면 true를 반환한다")
    void should_return_true_when_now_is_within_schedule() {
      // given
      Schedule schedule = schedule();
      LocalDateTime duringSchedule = START_AT.plusMinutes(30);

      // when
      boolean result = schedule.isOngoing(duringSchedule);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("현재 시간이 일정 시작 시간과 같으면 true를 반환한다")
    void should_return_true_when_now_equals_start_time() {
      // given
      Schedule schedule = schedule();

      // when
      boolean result = schedule.isOngoing(START_AT);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("현재 시간이 일정 종료 시간과 같으면 true를 반환한다")
    void should_return_true_when_now_equals_end_time() {
      // given
      Schedule schedule = schedule();

      // when
      boolean result = schedule.isOngoing(END_AT);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("현재 시간이 일정 시작 전이면 false를 반환한다")
    void should_return_false_when_now_is_before_schedule() {
      // given
      Schedule schedule = schedule();
      LocalDateTime beforeSchedule = START_AT.minusMinutes(1);

      // when
      boolean result = schedule.isOngoing(beforeSchedule);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("현재 시간이 일정 종료 후이면 false를 반환한다")
    void should_return_false_when_now_is_after_schedule() {
      // given
      Schedule schedule = schedule();
      LocalDateTime afterSchedule = END_AT.plusMinutes(1);

      // when
      boolean result = schedule.isOngoing(afterSchedule);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("시작 시간이 null이면 false를 반환한다")
    void should_return_false_when_start_time_is_null() {
      // given
      Schedule schedule = Schedule.of(ID, STUDY_ID, CREATOR_ID, TITLE, LOCATION_MEMO, null, END_AT);
      LocalDateTime now = LocalDateTime.now();

      // when
      boolean result = schedule.isOngoing(now);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("종료 시간이 null이면 false를 반환한다")
    void should_return_false_when_end_time_is_null() {
      // given
      Schedule schedule = Schedule.of(ID, STUDY_ID, CREATOR_ID, TITLE, LOCATION_MEMO, START_AT, null);
      LocalDateTime now = LocalDateTime.now();

      // when
      boolean result = schedule.isOngoing(now);

      // then
      assertThat(result).isFalse();
    }
  }
}
