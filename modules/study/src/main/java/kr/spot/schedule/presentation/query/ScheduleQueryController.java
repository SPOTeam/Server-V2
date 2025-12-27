package kr.spot.schedule.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import kr.spot.ApiResponse;
import kr.spot.code.status.SuccessStatus;
import kr.spot.schedule.application.query.GetScheduleService;
import kr.spot.schedule.presentation.query.dto.GetScheduleListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
@RequiredArgsConstructor
public class ScheduleQueryController {

  private final GetScheduleService getScheduleService;

  @Operation(summary = "월별 일정 조회", description = "해당 연/월의 모든 일정을 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @GetMapping("/monthly")
  public ResponseEntity<ApiResponse<GetScheduleListResponse>> getMonthlySchedules(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "연도", example = "2025") @RequestParam Integer year,
      @Parameter(description = "월", example = "1") @RequestParam Integer month) {
    GetScheduleListResponse response = getScheduleService.getMonthlySchedules(studyId, year, month);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }

  @Operation(summary = "주간 일정 조회", description = "해당 날짜가 속한 주(월~일)의 모든 일정을 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @GetMapping("/weekly")
  public ResponseEntity<ApiResponse<GetScheduleListResponse>> getWeeklySchedules(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "조회 기준 날짜 (해당 주 전체 조회)", example = "2025-01-15")
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
    GetScheduleListResponse response = getScheduleService.getWeeklySchedules(studyId, date);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }

  @Operation(summary = "다가오는 일정 조회", description = "현재 시점 이후의 가장 가까운 일정 2개를 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @GetMapping("/upcoming")
  public ResponseEntity<ApiResponse<GetScheduleListResponse>> getUpcomingSchedules(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId) {
    GetScheduleListResponse response = getScheduleService.getUpcomingSchedules(studyId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
