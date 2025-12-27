package kr.spot.schedule.application.command;

import kr.spot.IdGenerator;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageScheduleService {

  private final IdGenerator idGenerator;
  private final ScheduleRepository scheduleRepository;

  public void createSchedule(CreateScheduleRequest request, long studyId) {
    Schedule schedule = Schedule.of(idGenerator.nextId(), studyId, request.title(),
        request.locationInfo(),
        request.startAt(), request.endAt());

    scheduleRepository.save(schedule);
  }

  public void deleteSchedule(long studyId, long scheduleId) {
    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.delete(studyId);
  }
}
