package kr.spot.schedule.presentation.query.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GetScheduleListResponse(
    List<ScheduleResponse> schedules,
    long totalCount
) {

  public static GetScheduleListResponse from(List<ScheduleResponse> schedules) {
    return new GetScheduleListResponse(schedules, schedules.size());
  }

  public record ScheduleResponse(
      Long scheduleId,
      String title,
      LocalDateTime startAt,
      LocalDateTime endAt,
      boolean isNow,
      boolean isMine
  ) {

    public static ScheduleResponse from(Long scheduleId, String title,
        LocalDateTime startAt, LocalDateTime endAt, boolean isNow, boolean isMine) {
      return new ScheduleResponse(scheduleId, title, startAt, endAt, isNow, isMine);
    }

  }

}
