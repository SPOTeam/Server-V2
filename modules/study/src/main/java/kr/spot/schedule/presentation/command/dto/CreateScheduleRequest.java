package kr.spot.schedule.presentation.command.dto;

import java.time.LocalDateTime;

public record CreateScheduleRequest(
    String title,
    String locationInfo,
    LocalDateTime startAt,
    LocalDateTime endAt
) {

}
