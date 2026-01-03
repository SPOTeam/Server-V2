package kr.spot.schedule.presentation.command;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.code.status.SuccessStatus;
import kr.spot.schedule.application.command.ManageScheduleService;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;
import kr.spot.schedule.presentation.command.dto.CreateScheduleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
@RequiredArgsConstructor
public class ScheduleCommandController {

  private final ManageScheduleService manageScheduleService;

  @Operation(summary = "스터디 일정 생성", description = "스터디에 새로운 일정을 생성합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`STUDY404`: 스터디를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<CreateScheduleResponse>> createSchedule(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @RequestBody CreateScheduleRequest request) {
    long scheduleId = manageScheduleService.createSchedule(request, studyId);
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._CREATED, CreateScheduleResponse.from(scheduleId)));
  }

  @Operation(summary = "스터디 일정 삭제", description = "스터디의 일정을 삭제합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "일정 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`SCHEDULE403`: 해당 일정에 대한 접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`SCHEDULE404`: 일정을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<ApiResponse<Void>> deleteSchedule(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId) {
    manageScheduleService.deleteSchedule(studyId, scheduleId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
