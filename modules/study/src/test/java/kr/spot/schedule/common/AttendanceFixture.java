package kr.spot.schedule.common;

import java.time.LocalDateTime;
import kr.spot.schedule.domain.Attendance;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.domain.vo.MemberInfo;

public class AttendanceFixture {

  public static final long STUDY_ID = 1L;
  public static final long SCHEDULE_ID = 100L;
  public static final long ATTENDANCE_ID = 200L;
  public static final long MEMBER_ID = 42L;
  public static final long OTHER_MEMBER_ID = 99L;

  public static final String MEMBER_NAME = "테스터";
  public static final String MEMBER_PROFILE_URL = "https://example.com/profile.jpg";
  public static final String QR_CODE_URL = "https://example.com/qr.png";

  public static MemberInfo memberInfo() {
    return MemberInfo.of(MEMBER_ID, MEMBER_NAME, MEMBER_PROFILE_URL);
  }

  public static MemberInfo memberInfo(long memberId) {
    return MemberInfo.of(memberId, MEMBER_NAME, MEMBER_PROFILE_URL);
  }

  public static Attendance attendance() {
    return Attendance.of(ATTENDANCE_ID, SCHEDULE_ID, memberInfo());
  }

  public static Schedule ongoingSchedule() {
    LocalDateTime now = LocalDateTime.now();
    return Schedule.of(
        SCHEDULE_ID,
        STUDY_ID,
        "테스트 일정",
        "테스트 장소",
        now.minusHours(1),
        now.plusHours(1)
    );
  }

  public static Schedule ongoingScheduleWithAttendance() {
    Schedule schedule = ongoingSchedule();
    schedule.startAttendance(STUDY_ID);
    return schedule;
  }

  public static Schedule pastSchedule() {
    LocalDateTime now = LocalDateTime.now();
    return Schedule.of(
        SCHEDULE_ID,
        STUDY_ID,
        "지난 일정",
        "테스트 장소",
        now.minusHours(3),
        now.minusHours(1)
    );
  }

  public static Schedule futureSchedule() {
    LocalDateTime now = LocalDateTime.now();
    return Schedule.of(
        SCHEDULE_ID,
        STUDY_ID,
        "미래 일정",
        "테스트 장소",
        now.plusHours(1),
        now.plusHours(3)
    );
  }
}
