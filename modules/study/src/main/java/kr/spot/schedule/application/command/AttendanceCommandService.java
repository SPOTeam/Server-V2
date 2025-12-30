package kr.spot.schedule.application.command;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.schedule.application.event.AttendanceStartedEvent;
import kr.spot.schedule.domain.Attendance;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.domain.enums.AttendanceStatus;
import kr.spot.schedule.domain.vo.MemberInfo;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceCommandService {

  private final IdGenerator idGenerator;
  private final ScheduleRepository scheduleRepository;
  private final AttendanceRepository attendanceRepository;
  private final StudyAccessValidator studyAccessValidator;
  private final GetMemberInfoPort getMemberInfoPort;
  private final ApplicationEventPublisher eventPublisher;

  @Value("${app.base-url}")
  private String baseUrl;

  public void startAttendance(long studyId, long scheduleId, long memberId) {
    studyAccessValidator.validateStudyLeader(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.startAttendance(studyId);

    String qrContent = generateQrContent(studyId, scheduleId);
    eventPublisher.publishEvent(AttendanceStartedEvent.of(studyId, scheduleId, qrContent));
  }

  public void stopAttendance(long studyId, long scheduleId, long memberId) {
    studyAccessValidator.validateStudyLeader(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.stopAttendance(studyId);
  }

  public void checkAttendance(long studyId, long scheduleId, long memberId) {
    studyAccessValidator.validateStudyMember(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.validateAttendanceCheckable();

    if (attendanceRepository.existsByScheduleIdAndMemberInfoMemberId(scheduleId, memberId)) {
      throw new GeneralException(ErrorStatus._ATTENDANCE_ALREADY_CHECKED);
    }

    MemberInfo memberInfo = getMemberInfo(memberId);
    Attendance attendance = Attendance.of(idGenerator.nextId(), scheduleId, memberInfo);
    attendance.markAttendance(AttendanceStatus.PRESENT);
    attendanceRepository.save(attendance);
  }

  private MemberInfo getMemberInfo(long memberId) {
    Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoPort.getMemberInfo(
        List.of(memberId));
    MemberInfoResponse info = memberInfoMap.get(memberId);
    if (info == null) {
      throw new GeneralException(ErrorStatus._MEMBER_NOT_FOUND);
    }
    return MemberInfo.of(memberId, info.name(), info.profileImageUrl());
  }

  private String generateQrContent(long studyId, long scheduleId) {
    String token = UUID.randomUUID().toString();
    return String.format("%s/api/studies/%d/schedules/%d/attendance/check?token=%s",
        baseUrl, studyId, scheduleId, token);
  }
}
