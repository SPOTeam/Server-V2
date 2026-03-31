package kr.spot.schedule.application.command;

import static kr.spot.schedule.common.AttendanceFixture.MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.MEMBER_NAME;
import static kr.spot.schedule.common.AttendanceFixture.MEMBER_PROFILE_URL;
import static kr.spot.schedule.common.AttendanceFixture.OTHER_MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.SCHEDULE_ID;
import static kr.spot.schedule.common.AttendanceFixture.STUDY_ID;
import static kr.spot.schedule.common.AttendanceFixture.attendance;
import static kr.spot.schedule.common.AttendanceFixture.ongoingSchedule;
import static kr.spot.schedule.common.AttendanceFixture.ongoingScheduleWithAttendance;
import static kr.spot.schedule.common.AttendanceFixture.pastSchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.schedule.domain.Attendance;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.domain.enums.AttendanceStatus;
import kr.spot.schedule.infrastructure.crypto.AttendanceTokenEncryptor;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleExclusionRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceCommandServiceTest {

  private static final String ENCRYPTED_TOKEN = "encrypted_token";
  private static final String DECRYPTED_TOKEN = STUDY_ID + ":" + SCHEDULE_ID + ":1234567890";

  @Mock
  IdGenerator idGenerator;
  @Mock
  ScheduleRepository scheduleRepository;
  @Mock
  StudyRepository studyRepository;
  @Mock
  AttendanceRepository attendanceRepository;
  @Mock
  ScheduleExclusionRepository scheduleExclusionRepository;
  @Mock
  StudyMemberRepository studyMemberRepository;
  @Mock
  StudyAccessValidator studyAccessValidator;
  @Mock
  GetMemberInfoPort getMemberInfoPort;
  @Mock
  ApplicationEventPublisher eventPublisher;
  @Mock
  AttendanceTokenEncryptor tokenEncryptor;

  AttendanceCommandService service;

  @BeforeEach
  void setUp() {
    service = new AttendanceCommandService(
        idGenerator,
        scheduleRepository,
        studyRepository,
        attendanceRepository,
        scheduleExclusionRepository,
        studyMemberRepository,
        studyAccessValidator,
        getMemberInfoPort,
        eventPublisher,
        tokenEncryptor
    );
  }

  @Nested
  @DisplayName("출석체크 시작")
  class StartAttendance {

    @Test
    @DisplayName("스터디장이 출석체크를 시작하면 일정이 출석체크 활성 상태가 되고 모든 멤버의 출석 기록이 생성된다")
    void should_activateAttendanceAndCreateRecords_when_leaderStartsAttendance() {
      // given
      Schedule schedule = ongoingSchedule();
      StudyMember owner = StudyMember.create(1L, STUDY_ID, MEMBER_ID);
      StudyMember member = createApprovedMember(2L, STUDY_ID, OTHER_MEMBER_ID);
      MemberInfoResponse ownerInfo = new MemberInfoResponse(MEMBER_NAME, MEMBER_PROFILE_URL);
      MemberInfoResponse memberInfo = new MemberInfoResponse("멤버", MEMBER_PROFILE_URL);

      doNothing().when(studyAccessValidator).validateStudyLeader(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);
      when(studyMemberRepository.findAllByStudyIdAndStudyMemberStatusIn(any(), anyList()))
          .thenReturn(List.of(owner, member));
      when(getMemberInfoPort.getMemberInfo(anyList()))
          .thenReturn(Map.of(MEMBER_ID, ownerInfo, OTHER_MEMBER_ID, memberInfo));
      when(idGenerator.nextId()).thenReturn(100L, 101L);
      when(tokenEncryptor.encrypt(any())).thenReturn(ENCRYPTED_TOKEN);

      // when
      service.startAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      assertThat(schedule.isAttendanceActive()).isTrue();
      verify(eventPublisher).publishEvent(any(Object.class));

      ArgumentCaptor<List<Attendance>> captor = ArgumentCaptor.forClass(List.class);
      verify(attendanceRepository).saveAll(captor.capture());

      List<Attendance> savedAttendances = captor.getValue();
      assertThat(savedAttendances).hasSize(2);

      Attendance ownerAttendance = savedAttendances.stream()
          .filter(a -> a.getMemberInfo().getMemberId().equals(MEMBER_ID))
          .findFirst()
          .orElseThrow();
      assertThat(ownerAttendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);

      Attendance memberAttendance = savedAttendances.stream()
          .filter(a -> a.getMemberInfo().getMemberId().equals(OTHER_MEMBER_ID))
          .findFirst()
          .orElseThrow();
      assertThat(memberAttendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.UNDECIDED);
    }

    @Test
    @DisplayName("스터디장이 아니면 출석체크를 시작할 수 없다")
    void should_throwException_when_notStudyLeader() {
      // given
      doThrow(new GeneralException(ErrorStatus._ONLY_LEADER_CAN_ACCESS))
          .when(studyAccessValidator).validateStudyLeader(STUDY_ID, OTHER_MEMBER_ID);

      // when & then
      assertThatThrownBy(() -> service.startAttendance(STUDY_ID, SCHEDULE_ID, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }

    @Test
    @DisplayName("이미 출석체크가 진행 중이면 예외를 던진다")
    void should_throwException_when_attendanceAlreadyActive() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      doNothing().when(studyAccessValidator).validateStudyLeader(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.startAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._SCHEDULE_QR_CODE_ALREADY_ASSIGNED);
    }

    @Test
    @DisplayName("일정 시간이 아니면 출석체크를 시작할 수 없다")
    void should_throwException_when_scheduleNotOngoing() {
      // given
      Schedule schedule = pastSchedule();

      doNothing().when(studyAccessValidator).validateStudyLeader(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.startAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }
  }

  @Nested
  @DisplayName("출석체크 종료")
  class StopAttendance {

    @Test
    @DisplayName("스터디장이 출석체크를 종료하면 비활성 상태가 된다")
    void should_deactivateAttendance_when_leaderStopsAttendance() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      doNothing().when(studyAccessValidator).validateStudyLeader(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when
      service.stopAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      assertThat(schedule.isAttendanceActive()).isFalse();
      assertThat(schedule.getAttendanceQrCodeImageUrl()).isNull();
    }

    @Test
    @DisplayName("스터디장이 아니면 출석체크를 종료할 수 없다")
    void should_throwException_when_notStudyLeader() {
      // given
      doThrow(new GeneralException(ErrorStatus._ONLY_LEADER_CAN_ACCESS))
          .when(studyAccessValidator).validateStudyLeader(STUDY_ID, OTHER_MEMBER_ID);

      // when & then
      assertThatThrownBy(() -> service.stopAttendance(STUDY_ID, SCHEDULE_ID, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }
  }

  @Nested
  @DisplayName("출석체크 처리")
  class CheckAttendance {

    @Test
    @DisplayName("스터디 멤버가 토큰으로 출석체크를 하면 출석 상태가 PRESENT로 변경된다")
    void should_markAttendancePresent_when_memberChecksInWithToken() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();
      Attendance existingAttendance = attendance();

      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN)).thenReturn(DECRYPTED_TOKEN);
      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);
      when(attendanceRepository.findByScheduleIdAndMemberInfoMemberId(SCHEDULE_ID, MEMBER_ID))
          .thenReturn(Optional.of(existingAttendance));

      // when
      service.checkAttendance(ENCRYPTED_TOKEN, MEMBER_ID);

      // then
      assertThat(existingAttendance.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
      assertThat(existingAttendance.getAttendedAt()).isNotNull();
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 출석체크를 할 수 없다")
    void should_throwException_when_notStudyMember() {
      // given
      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN)).thenReturn(DECRYPTED_TOKEN);
      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, OTHER_MEMBER_ID);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(ENCRYPTED_TOKEN, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("출석체크가 시작되지 않았으면 출석할 수 없다")
    void should_throwException_when_attendanceNotStarted() {
      // given
      Schedule schedule = ongoingSchedule();

      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN)).thenReturn(DECRYPTED_TOKEN);
      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(ENCRYPTED_TOKEN, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_STARTED);
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 예외를 던진다")
    void should_throwException_when_invalidToken() {
      // given
      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN))
          .thenThrow(new GeneralException(ErrorStatus._INVALID_ATTENDANCE_TOKEN));

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(ENCRYPTED_TOKEN, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._INVALID_ATTENDANCE_TOKEN);
    }

    @Test
    @DisplayName("출석 기록이 없으면 예외를 던진다")
    void should_throwException_when_attendanceRecordNotFound() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN)).thenReturn(DECRYPTED_TOKEN);
      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);
      when(attendanceRepository.findByScheduleIdAndMemberInfoMemberId(SCHEDULE_ID, MEMBER_ID))
          .thenReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(ENCRYPTED_TOKEN, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._STUDY_MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("일정 시간이 아니면 출석체크를 할 수 없다")
    void should_throwException_when_scheduleTimeExpired() {
      // given
      Schedule schedule = pastSchedule();
      ReflectionTestUtils.setField(schedule, "attendanceActive", true);

      when(tokenEncryptor.decrypt(ENCRYPTED_TOKEN)).thenReturn(DECRYPTED_TOKEN);
      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(ENCRYPTED_TOKEN, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }
  }

  private StudyMember createApprovedMember(long id, long studyId, long memberId) {
    StudyMember member = StudyMember.apply(id, studyId, memberId, "테스트");
    ReflectionTestUtils.setField(member, "studyMemberStatus", StudyMemberStatus.APPROVED);
    return member;
  }
}
