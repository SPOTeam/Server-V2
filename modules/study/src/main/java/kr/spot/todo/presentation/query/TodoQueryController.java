package kr.spot.todo.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import kr.spot.ApiResponse;
import kr.spot.code.status.SuccessStatus;
import kr.spot.todo.application.query.GetTodoService;
import kr.spot.todo.presentation.query.dto.GetTodoListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 투두")
@RestController
@RequestMapping("/api/studies/{studyId}/todos")
@RequiredArgsConstructor
public class TodoQueryController {

  private final GetTodoService getTodoService;

  @Operation(summary = "날짜별 투두 조회", description = "특정 날짜의 투두를 조회합니다. 미완료 항목이 먼저, 완료 항목은 별도로 조회됩니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @GetMapping("/members/{memberId}")
  public ResponseEntity<ApiResponse<GetTodoListResponse>> getTodosByDate(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @PathVariable @Parameter(hidden = true) Long memberId,
      @Parameter(description = "조회할 날짜 (ISO 8601 표준)", example = "2025-01-15")
      @RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
    GetTodoListResponse response = getTodoService.getTodosByDate(studyId, memberId, date);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
