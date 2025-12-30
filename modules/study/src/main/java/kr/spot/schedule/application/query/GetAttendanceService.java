package kr.spot.schedule.application.query;

import java.util.List;
import kr.spot.schedule.domain.Attendance;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.query.dto.GetAttendanceInfoResponse;
import kr.spot.schedule.presentation.query.dto.GetAttendanceListResponse;
import kr.spot.schedule.presentation.query.dto.GetAttendanceListResponse.AttendanceInfoResponse;
import kr.spot.schedule.presentation.query.dto.GetAttendanceListResponse.MemberInfoResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetAttendanceService {

  private final ScheduleRepository scheduleRepository;
  private final AttendanceRepository attendanceRepository;
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

  public GetAttendanceListResponse getAttendanceList(long studyId, long scheduleId,
      long memberId) {
    studyAccessValidator.validateStudyMember(studyId, memberId);

    List<Attendance> attendances = attendanceRepository.findAllByScheduleId(scheduleId);

    List<AttendanceInfoResponse> attendanceInfos = attendances.stream()
        .map(attendance -> AttendanceInfoResponse.from(
            MemberInfoResponse.from(
                attendance.getMemberInfo().getMemberId(),
                attendance.getMemberInfo().getMemberName(),
                attendance.getMemberInfo().getMemberProfileImageUrl()
            ),
            attendance.getAttendanceStatus().name(),
            attendance.getAttendedAt()
        ))
        .toList();

    return GetAttendanceListResponse.from(attendanceInfos, attendanceInfos.size());
  }
}
