package kr.spot.schedule.application.command;

import java.time.Instant;
import java.util.List;
import java.util.Map;
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
import kr.spot.schedule.infrastructure.crypto.AttendanceTokenEncryptor;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AttendanceCommandService {

  private static final List<StudyMemberStatus> ACTIVE_MEMBER_STATUSES =
      List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED);

  private final IdGenerator idGenerator;
  private final ScheduleRepository scheduleRepository;
  private final AttendanceRepository attendanceRepository;
  private final StudyMemberRepository studyMemberRepository;
  private final StudyAccessValidator studyAccessValidator;
  private final GetMemberInfoPort getMemberInfoPort;
  private final ApplicationEventPublisher eventPublisher;
  private final AttendanceTokenEncryptor tokenEncryptor;

  public void startAttendance(long studyId, long scheduleId, long memberId) {
    studyAccessValidator.validateStudyLeader(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.startAttendance(studyId);

    createAttendancesForAllMembers(studyId, scheduleId);

    String qrContent = generateEncryptedToken(studyId, scheduleId);
    eventPublisher.publishEvent(AttendanceStartedEvent.of(studyId, scheduleId, qrContent));
  }

  public void stopAttendance(long studyId, long scheduleId, long memberId) {
    studyAccessValidator.validateStudyLeader(studyId, memberId);

    Schedule schedule = scheduleRepository.getById(scheduleId);
    schedule.stopAttendance(studyId);
  }

  public void checkAttendance(String encryptedToken, long memberId) {
    AttendanceTokenData tokenData = decryptToken(encryptedToken);

    studyAccessValidator.validateStudyMember(tokenData.studyId(), memberId);

    Schedule schedule = scheduleRepository.getById(tokenData.scheduleId());
    schedule.validateAttendanceCheckable();

    Attendance attendance = attendanceRepository
        .findByScheduleIdAndMemberInfoMemberId(tokenData.scheduleId(), memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_MEMBER_NOT_FOUND));

    attendance.markAttendance(AttendanceStatus.PRESENT);
  }

  private void createAttendancesForAllMembers(long studyId, long scheduleId) {
    List<StudyMember> activeMembers = studyMemberRepository
        .findAllByStudyIdAndStudyMemberStatusIn(studyId, ACTIVE_MEMBER_STATUSES);

    List<Long> memberIds = activeMembers.stream()
        .map(StudyMember::getMemberId)
        .toList();

    Map<Long, MemberInfoResponse> memberInfoMap = getMemberInfoPort.getMemberInfo(memberIds);

    List<Attendance> attendances = activeMembers.stream()
        .map(studyMember -> {
          MemberInfoResponse info = memberInfoMap.get(studyMember.getMemberId());
          MemberInfo memberInfo = MemberInfo.of(
              studyMember.getMemberId(),
              info.name(),
              info.profileImageUrl()
          );
          return Attendance.createPending(idGenerator.nextId(), scheduleId, memberInfo);
        })
        .toList();

    attendanceRepository.saveAll(attendances);
  }

  private String generateEncryptedToken(long studyId, long scheduleId) {
    String plainText = String.format("%d:%d:%d", studyId, scheduleId, Instant.now().toEpochMilli());
    return tokenEncryptor.encrypt(plainText);
  }

  private AttendanceTokenData decryptToken(String encryptedToken) {
    String plainText = tokenEncryptor.decrypt(encryptedToken);
    String[] parts = plainText.split(":");
    if (parts.length != 3) {
      throw new GeneralException(ErrorStatus._INVALID_ATTENDANCE_TOKEN);
    }
    return new AttendanceTokenData(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
  }

  private record AttendanceTokenData(long studyId, long scheduleId) {

  }
}
