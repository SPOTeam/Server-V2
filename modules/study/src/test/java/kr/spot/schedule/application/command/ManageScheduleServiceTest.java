package kr.spot.schedule.application.command;

import static kr.spot.schedule.common.ScheduleFixture.CREATOR_ID;
import static kr.spot.schedule.common.ScheduleFixture.LOCATION_MEMO;
import static kr.spot.schedule.common.ScheduleFixture.STUDY_ID;
import static kr.spot.schedule.common.ScheduleFixture.TITLE;
import static kr.spot.schedule.common.ScheduleFixture.createScheduleRequest;
import static kr.spot.schedule.common.ScheduleFixture.schedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageScheduleServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  ScheduleRepository scheduleRepository;

  @Captor
  ArgumentCaptor<Schedule> scheduleCaptor;

  ManageScheduleService manageScheduleService;

  @BeforeEach
  void setUp() {
    manageScheduleService = new ManageScheduleService(idGenerator, scheduleRepository);
  }

  @Nested
  @DisplayName("일정 생성 (createSchedule)")
  class CreateSchedule {

    @Test
    @DisplayName("일정을 정상적으로 생성할 수 있다")
    void should_create_schedule_successfully() {
      // given
      Long generatedId = 1L;
      CreateScheduleRequest request = createScheduleRequest();

      when(idGenerator.nextId()).thenReturn(generatedId);
      when(scheduleRepository.save(any(Schedule.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageScheduleService.createSchedule(request, STUDY_ID, CREATOR_ID);

      // then
      verify(scheduleRepository).save(scheduleCaptor.capture());

      Schedule capturedSchedule = scheduleCaptor.getValue();
      assertThat(capturedSchedule.getId()).isEqualTo(generatedId);
      assertThat(capturedSchedule.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(capturedSchedule.getCreatorId()).isEqualTo(CREATOR_ID);
      assertThat(capturedSchedule.getTitle()).isEqualTo(TITLE);
      assertThat(capturedSchedule.getLocationMemo()).isEqualTo(LOCATION_MEMO);
      assertThat(capturedSchedule.getStartAt()).isEqualTo(request.startAt());
      assertThat(capturedSchedule.getEndAt()).isEqualTo(request.endAt());
    }

    @Test
    @DisplayName("위치 정보 없이 일정을 생성할 수 있다")
    void should_create_schedule_without_location() {
      // given
      Long generatedId = 1L;
      CreateScheduleRequest request = new CreateScheduleRequest(
          TITLE, null, createScheduleRequest().startAt(), createScheduleRequest().endAt()
      );

      when(idGenerator.nextId()).thenReturn(generatedId);
      when(scheduleRepository.save(any(Schedule.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageScheduleService.createSchedule(request, STUDY_ID, CREATOR_ID);

      // then
      verify(scheduleRepository).save(scheduleCaptor.capture());

      Schedule capturedSchedule = scheduleCaptor.getValue();
      assertThat(capturedSchedule.getLocationMemo()).isNull();
    }
  }

  @Nested
  @DisplayName("일정 삭제 (deleteSchedule)")
  class DeleteSchedule {

    @Test
    @DisplayName("일정을 정상적으로 삭제할 수 있다")
    void should_delete_schedule_successfully() {
      // given
      long scheduleId = 1L;
      Schedule schedule = schedule(scheduleId, STUDY_ID);

      when(scheduleRepository.getById(anyLong())).thenReturn(schedule);

      // when
      manageScheduleService.deleteSchedule(STUDY_ID, scheduleId);

      // then
      verify(scheduleRepository).getById(scheduleId);
    }

    @Test
    @DisplayName("다른 스터디의 일정을 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_other_study_schedule() {
      // given
      long scheduleId = 1L;
      long otherStudyId = 999L;
      Schedule schedule = schedule(scheduleId, STUDY_ID);

      when(scheduleRepository.getById(anyLong())).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> manageScheduleService.deleteSchedule(otherStudyId, scheduleId))
          .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("존재하지 않는 일정을 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_schedule_not_found() {
      // given
      long scheduleId = 999L;

      when(scheduleRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._SCHEDULE_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> manageScheduleService.deleteSchedule(STUDY_ID, scheduleId))
          .isInstanceOf(GeneralException.class);
    }
  }
}
