package kr.spot.schedule.application.query;

import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.query.dto.GetAttendanceInfoResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAttendanceService {

  private final ScheduleRepository scheduleRepository;
  private final StudyAccessValidator studyAccessValidator;

  public GetAttendanceInfoResponse getAttendanceQrCode(long studyId, long scheduleId,
      long memberId) {
    studyAccessValidator.validateStudyMember(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    return GetAttendanceInfoResponse.of(
        schedule.isAttendanceActive(),
        schedule.getAttendanceQrCodeImageUrl()
    );
  }
}
