package kr.spot.schedule.common;

import java.time.LocalDateTime;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;

public class ScheduleFixture {

  public static final Long ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final String TITLE = "Weekly Meeting";
  public static final String LOCATION_MEMO = "강남역 스터디카페";
  public static final LocalDateTime START_AT = LocalDateTime.of(2025, 1, 15, 14, 0);
  public static final LocalDateTime END_AT = LocalDateTime.of(2025, 1, 15, 16, 0);

  public static Schedule schedule() {
    return Schedule.of(ID, STUDY_ID, TITLE, LOCATION_MEMO, START_AT, END_AT);
  }

  public static Schedule schedule(Long id, Long studyId) {
    return Schedule.of(id, studyId, TITLE, LOCATION_MEMO, START_AT, END_AT);
  }

  public static CreateScheduleRequest createScheduleRequest() {
    return new CreateScheduleRequest(TITLE, LOCATION_MEMO, START_AT, END_AT);
  }

  public static CreateScheduleRequest createScheduleRequest(String title) {
    return new CreateScheduleRequest(title, LOCATION_MEMO, START_AT, END_AT);
  }
}