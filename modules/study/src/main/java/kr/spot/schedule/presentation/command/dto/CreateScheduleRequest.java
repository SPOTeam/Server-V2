package kr.spot.schedule.presentation.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "스터디 일정 생성 요청")
public record CreateScheduleRequest(
    @Schema(description = "일정 제목", example = "주간 스터디 모임", requiredMode = Schema.RequiredMode.REQUIRED)
    String title,

    @Schema(description = "장소 정보", example = "강남역 스터디카페 3층")
    String locationInfo,

    @Schema(description = "시작 일시", example = "2025-01-15T14:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료 일시", example = "2025-01-15T16:00:00")
    LocalDateTime endAt
) {

}
