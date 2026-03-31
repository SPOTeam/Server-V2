package kr.spot.schedule.application.query;

import static kr.spot.schedule.common.ScheduleFixture.CREATOR_ID;
import static kr.spot.schedule.common.ScheduleFixture.STUDY_ID;
import static kr.spot.schedule.common.ScheduleFixture.schedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleExclusionRepository;
import kr.spot.schedule.infrastructure.jpa.querydsl.ScheduleQueryRepository;
import kr.spot.schedule.presentation.query.dto.GetScheduleListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetScheduleServiceTest {

  @Mock
  ScheduleQueryRepository scheduleQueryRepository;

  @Mock
  ScheduleExclusionRepository scheduleExclusionRepository;

  GetScheduleService getScheduleService;

  @BeforeEach
  void setUp() {
    getScheduleService = new GetScheduleService(scheduleQueryRepository, scheduleExclusionRepository);
  }

  @Nested
  @DisplayName("월별 일정 조회 (getMonthlySchedules)")
  class GetMonthlySchedules {

    @Test
    @DisplayName("월별 일정을 조회할 수 있다")
    void should_get_monthly_schedules() {
      // given
      int year = 2025;
      int month = 1;
      List<Schedule> schedules = List.of(
          schedule(1L, STUDY_ID),
          schedule(2L, STUDY_ID)
      );

      when(scheduleQueryRepository.findMonthlySchedules(anyLong(), any(LocalDate.class)))
          .thenReturn(schedules);
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getMonthlySchedules(STUDY_ID, year, month, CREATOR_ID);

      // then
      assertThat(response.schedules()).hasSize(2);
      assertThat(response.totalCount()).isEqualTo(2);
      verify(scheduleQueryRepository).findMonthlySchedules(STUDY_ID, LocalDate.of(year, month, 1));
    }

    @Test
    @DisplayName("일정이 없으면 빈 리스트를 반환한다")
    void should_return_empty_list_when_no_schedules() {
      // given
      when(scheduleQueryRepository.findMonthlySchedules(anyLong(), any(LocalDate.class)))
          .thenReturn(Collections.emptyList());
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getMonthlySchedules(STUDY_ID, 2025, 1, CREATOR_ID);

      // then
      assertThat(response.schedules()).isEmpty();
      assertThat(response.totalCount()).isZero();
    }
  }

  @Nested
  @DisplayName("주간 일정 조회 (getWeeklySchedules)")
  class GetWeeklySchedules {

    @Test
    @DisplayName("주간 일정을 조회할 수 있다")
    void should_get_weekly_schedules() {
      // given
      LocalDate date = LocalDate.of(2025, 1, 15);
      List<Schedule> schedules = List.of(schedule(1L, STUDY_ID));

      when(scheduleQueryRepository.findWeeklySchedules(anyLong(), any(LocalDate.class)))
          .thenReturn(schedules);
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getWeeklySchedules(STUDY_ID, date, CREATOR_ID);

      // then
      assertThat(response.schedules()).hasSize(1);
      verify(scheduleQueryRepository).findWeeklySchedules(STUDY_ID, date);
    }

    @Test
    @DisplayName("일정이 없으면 빈 리스트를 반환한다")
    void should_return_empty_list_when_no_schedules() {
      // given
      LocalDate date = LocalDate.of(2025, 1, 15);

      when(scheduleQueryRepository.findWeeklySchedules(anyLong(), any(LocalDate.class)))
          .thenReturn(Collections.emptyList());
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getWeeklySchedules(STUDY_ID, date, CREATOR_ID);

      // then
      assertThat(response.schedules()).isEmpty();
      assertThat(response.totalCount()).isZero();
    }
  }

  @Nested
  @DisplayName("다가오는 일정 조회 (getUpcomingSchedules)")
  class GetUpcomingSchedules {

    @Test
    @DisplayName("다가오는 일정을 조회할 수 있다")
    void should_get_upcoming_schedules() {
      // given
      List<Schedule> schedules = List.of(
          schedule(1L, STUDY_ID),
          schedule(2L, STUDY_ID)
      );

      when(scheduleQueryRepository.findUpcomingSchedules(anyLong(), anyInt()))
          .thenReturn(schedules);
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getUpcomingSchedules(STUDY_ID, CREATOR_ID);

      // then
      assertThat(response.schedules()).hasSize(2);
      verify(scheduleQueryRepository).findUpcomingSchedules(STUDY_ID, 2);
    }

    @Test
    @DisplayName("일정이 없으면 빈 리스트를 반환한다")
    void should_return_empty_list_when_no_schedules() {
      // given
      when(scheduleQueryRepository.findUpcomingSchedules(anyLong(), anyInt()))
          .thenReturn(Collections.emptyList());
      when(scheduleExclusionRepository.findExcludedScheduleIds(anyLong(), anyList()))
          .thenReturn(Collections.emptyList());

      // when
      GetScheduleListResponse response = getScheduleService.getUpcomingSchedules(STUDY_ID, CREATOR_ID);

      // then
      assertThat(response.schedules()).isEmpty();
      assertThat(response.totalCount()).isZero();
    }
  }
}
