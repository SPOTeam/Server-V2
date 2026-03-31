package kr.spot.schedule.application.command;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.domain.ScheduleExclusion;
import kr.spot.schedule.infrastructure.jpa.ScheduleExclusionRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;
import kr.spot.study.domain.Study;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageScheduleService {

  private final IdGenerator idGenerator;
  private final ScheduleRepository scheduleRepository;
  private final ScheduleExclusionRepository scheduleExclusionRepository;
  private final StudyRepository studyRepository;

  public long createSchedule(CreateScheduleRequest request, long studyId, long creatorId) {
    validateScheduleTimeConflict(request, studyId);

    long scheduleId = idGenerator.nextId();
    Schedule schedule = Schedule.of(scheduleId, studyId, creatorId, request.title(),
        request.locationInfo(),
        request.startAt(), request.endAt());

    scheduleRepository.save(schedule);
    return scheduleId;
  }

  public void deleteSchedule(long studyId, long scheduleId, long memberId) {
    Study study = studyRepository.getStudyById(studyId);

    if (study.getLeaderId() == memberId) {
      Schedule schedule = scheduleRepository.getById(scheduleId);
      schedule.delete(studyId);
    } else {
      excludeMemberFromSchedule(scheduleId, memberId);
    }
  }

  private void excludeMemberFromSchedule(long scheduleId, long memberId) {
    if (scheduleExclusionRepository.existsByScheduleIdAndMemberId(scheduleId, memberId)) {
      return;
    }
    ScheduleExclusion exclusion = ScheduleExclusion.of(
        idGenerator.nextId(), scheduleId, memberId);
    scheduleExclusionRepository.save(exclusion);
  }

  private void validateScheduleTimeConflict(CreateScheduleRequest request, long studyId) {
    boolean hasConflict = scheduleRepository.existsByStudyIdAndStartAtLessThanAndEndAtGreaterThan(
        studyId, request.endAt(), request.startAt());

    if (hasConflict) {
      throw new GeneralException(ErrorStatus._SCHEDULE_TIME_CONFLICT);
    }
  }
}
