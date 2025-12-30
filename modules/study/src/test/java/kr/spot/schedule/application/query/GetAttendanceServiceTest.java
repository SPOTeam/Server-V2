package kr.spot.schedule.application.query;

import static kr.spot.schedule.common.AttendanceFixture.MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.OTHER_MEMBER_ID;
import static kr.spot.schedule.common.AttendanceFixture.QR_CODE_URL;
import static kr.spot.schedule.common.AttendanceFixture.SCHEDULE_ID;
import static kr.spot.schedule.common.AttendanceFixture.STUDY_ID;
import static kr.spot.schedule.common.AttendanceFixture.ongoingSchedule;
import static kr.spot.schedule.common.AttendanceFixture.ongoingScheduleWithAttendance;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.presentation.query.dto.GetAttendanceInfoResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GetAttendanceServiceTest {

  @Mock
  ScheduleRepository scheduleRepository;
  @Mock
  StudyAccessValidator studyAccessValidator;

  GetAttendanceService service;

  @BeforeEach
  void setUp() {
    service = new GetAttendanceService(scheduleRepository, studyAccessValidator);
  }

  @Nested
  @DisplayName("출석체크 QR 코드 조회")
  class GetAttendanceQrCode {

    @Test
    @DisplayName("출석체크가 활성화된 경우 QR 코드 URL을 반환한다")
    void should_returnQrCodeUrl_when_attendanceActive() {
      // given
      Schedule schedule = ongoingScheduleWithAttendance();
      ReflectionTestUtils.setField(schedule, "attendanceQrCodeImageUrl", QR_CODE_URL);

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when
      GetAttendanceInfoResponse response = service.getAttendanceQrCode(
          STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      assertThat(response.attendanceActive()).isTrue();
      assertThat(response.qrCodeImageUrl()).isEqualTo(QR_CODE_URL);
    }

    @Test
    @DisplayName("출석체크가 비활성화된 경우 isActive가 false이다")
    void should_returnInactive_when_attendanceNotActive() {
      // given
      Schedule schedule = ongoingSchedule();

      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID)).thenReturn(schedule);

      // when
      GetAttendanceInfoResponse response = service.getAttendanceQrCode(
          STUDY_ID, SCHEDULE_ID, MEMBER_ID);

      // then
      assertThat(response.attendanceActive()).isFalse();
      assertThat(response.qrCodeImageUrl()).isNull();
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 QR 코드를 조회할 수 없다")
    void should_throwException_when_notStudyMember() {
      // given
      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, OTHER_MEMBER_ID);

      // when & then
      assertThatThrownBy(
          () -> service.getAttendanceQrCode(STUDY_ID, SCHEDULE_ID, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 일정은 조회할 수 없다")
    void should_throwException_when_scheduleNotFound() {
      // given
      doNothing().when(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      when(scheduleRepository.getById(SCHEDULE_ID))
          .thenThrow(new GeneralException(ErrorStatus._SCHEDULE_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> service.getAttendanceQrCode(STUDY_ID, SCHEDULE_ID, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .extracting(e -> ((GeneralException) e).getStatus())
          .isEqualTo(ErrorStatus._SCHEDULE_NOT_FOUND);
    }
  }
}
