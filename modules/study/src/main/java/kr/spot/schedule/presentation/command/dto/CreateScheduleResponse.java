package kr.spot.schedule.presentation.command.dto;

public record CreateScheduleResponse(Long scheduleId) {

  public static CreateScheduleResponse from(long scheduleId) {
    return new CreateScheduleResponse(scheduleId);
  }
}
