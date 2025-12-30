package kr.spot.schedule.application.command;

import static kr.spot.schedule.common.AttendanceFixture.MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.MEMBER_NAME;
import static kr.spot.schedule.common.AttendanceFixture.MEMBER_PROFILE_URL;
import static kr.spot.schedule.common.AttendanceFixture.OTHER_MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.SCHEDULE_ID;
import static kr.spot.schedule.common.AttendanceFixture.STUDY_ID;
import static kr.spot.schedule.common.AttendanceFixture.ongoingSchedule;
import static kr.spot.schedule.common.AttendanceFixture.ongoingScheduleWithAttendance;
import static kr.spot.schedule.common.AttendanceFixture.pastSchedule;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.schedule.domain.Attendance;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.domain.enums.AttendanceStatus;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
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

  @Mock
  IdGenerator idGenerator;
  @Mock
  ScheduleRepository scheduleRepository;
  @Mock
  AttendanceRepository attendanceRepository;
  @Mock
  StudyAccessValidator studyAccessValidator;
  @Mock
  GetMemberInfoPort getMemberInfoPort;
  @Mock
  ApplicationEventPublisher eventPublisher;

  AttendanceCommandService service;

  @BeforeEach
  void setUp() {
    service = new AttendanceCommandService(
        idGenerator,
        scheduleRepository,
        attendanceRepository,
        studyAccessValidator,
        getMemberInfoPort,
        eventPublisher
    );
    ReflectionTestUtils.setField(service, "baseUrl", "https://spot.com");
  }

  @Nested
  @DisplayName("출석체크 시작")
  class StartAttendance {

    @Test
    @DisplayName("스터디장이 출석체크를 시작하면 일정이 출석체크 활성 상태가 된다")
    void should_activateAttendance_when_leaderStartsAttendance() {
      // given
      Schedule schedule = ongoingSchedule();

      doNothing().when(studyAccessValidator).validateStudyLeader(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when
      service.startAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      assertThat(schedule.isAttendanceActive()).isTrue();
      verify(eventPublisher).publishEvent(any(Object.class));
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
    @DisplayName("스터디 멤버가 출석체크를 하면 출석 기록이 저장된다")
    void should_saveAttendance_when_memberChecksIn() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();
      MemberInfoResponse memberInfo = new MemberInfoResponse(MEMBER_NAME, MEMBER_PROFILE_URL);

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);
      when(attendanceRepository.existsByScheduleIdAndMemberInfoMemberId(SCHEDULE_ID, MEMBER_ID))
          .thenReturn(false);
      when(getMemberInfoPort.getMemberInfo(any())).thenReturn(Map.of(MEMBER_ID, memberInfo));
      when(idGenerator.nextId()).thenReturn(200L);

      // when
      service.checkAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
      verify(attendanceRepository).save(captor.capture());

      Attendance saved = captor.getValue();
      assertThat(saved.getScheduleId()).isEqualTo(SCHEDULE_ID);
      assertThat(saved.getMemberInfo().getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(saved.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 출석체크를 할 수 없다")
    void should_throwException_when_notStudyMember() {
      // given
      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, OTHER_MEMBER_ID);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(STUDY_ID, SCHEDULE_ID, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("출석체크가 시작되지 않았으면 출석할 수 없다")
    void should_throwException_when_attendanceNotStarted() {
      // given
      Schedule schedule = ongoingSchedule();

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_STARTED);
    }

    @Test
    @DisplayName("이미 출석체크를 했으면 중복 출석할 수 없다")
    void should_throwException_when_alreadyCheckedIn() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);
      when(attendanceRepository.existsByScheduleIdAndMemberInfoMemberId(SCHEDULE_ID, MEMBER_ID))
          .thenReturn(true);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_ALREADY_CHECKED);
    }

    @Test
    @DisplayName("일정 시간이 아니면 출석체크를 할 수 없다")
    void should_throwException_when_scheduleTimeExpired() {
      // given
      Schedule schedule = pastSchedule();
      // 과거 일정이지만 강제로 출석체크 활성화 상태로 설정 (테스트용)
      ReflectionTestUtils.setField(schedule, "attendanceActive", true);

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when & then
      assertThatThrownBy(() -> service.checkAttendance(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }
  }
}
