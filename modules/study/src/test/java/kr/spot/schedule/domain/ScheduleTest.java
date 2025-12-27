package kr.spot.schedule.domain;

import static kr.spot.schedule.common.ScheduleFixture.END_AT;
import static kr.spot.schedule.common.ScheduleFixture.ID;
import static kr.spot.schedule.common.ScheduleFixture.LOCATION_MEMO;
import static kr.spot.schedule.common.ScheduleFixture.START_AT;
import static kr.spot.schedule.common.ScheduleFixture.STUDY_ID;
import static kr.spot.schedule.common.ScheduleFixture.TITLE;
import static kr.spot.schedule.common.ScheduleFixture.schedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
      Schedule schedule = Schedule.of(ID, STUDY_ID, TITLE, LOCATION_MEMO, START_AT, END_AT);

      // then
      assertThat(schedule).isNotNull();
      assertThat(schedule.getId()).isEqualTo(ID);
      assertThat(schedule.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(schedule.getTitle()).isEqualTo(TITLE);
      assertThat(schedule.getLocationMemo()).isEqualTo(LOCATION_MEMO);
      assertThat(schedule.getStartAt()).isEqualTo(START_AT);
      assertThat(schedule.getEndAt()).isEqualTo(END_AT);
    }

    @Test
    @DisplayName("위치 정보 없이 일정 객체를 생성할 수 있다")
    void should_create_schedule_without_location() {
      // when
      Schedule schedule = Schedule.of(ID, STUDY_ID, TITLE, null, START_AT, END_AT);

      // then
      assertThat(schedule).isNotNull();
      assertThat(schedule.getLocationMemo()).isNull();
    }

    @Test
    @DisplayName("시작/종료 시간 없이 일정 객체를 생성할 수 있다")
    void should_create_schedule_without_time() {
      // when
      Schedule schedule = Schedule.of(ID, STUDY_ID, TITLE, LOCATION_MEMO, null, null);

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
}
