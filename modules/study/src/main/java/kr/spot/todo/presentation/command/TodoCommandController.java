package kr.spot.todo.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.todo.application.command.ManageTodoService;
import kr.spot.todo.presentation.command.dto.CreateTodoRequest;
import kr.spot.todo.presentation.command.dto.UpdateTodoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 투두")
@RestController
@RequestMapping("/api/studies/{studyId}/todos")
@RequiredArgsConstructor
public class TodoCommandController {

  private final ManageTodoService manageTodoService;

  @Operation(summary = "투두 생성", description = "스터디에 새로운 투두를 생성합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투두 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`STUDY404`: 스터디를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createTodo(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestBody CreateTodoRequest request) {
    manageTodoService.createTodo(studyId, memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @Operation(summary = "투두 수정", description = "투두 내용과 마감일을 수정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투두 수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`TODO4030`: 투두 수정은 작성자만 가능합니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`TODO404`: 투두를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PatchMapping("/{todoId}")
  public ResponseEntity<ApiResponse<Void>> updateTodo(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "투두 ID", required = true) @PathVariable Long todoId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestBody UpdateTodoRequest request) {
    manageTodoService.updateTodo(studyId, todoId, memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "투두 완료", description = "투두를 완료 상태로 변경합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투두 완료 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`TODO4030`: 투두 수정은 작성자만 가능합니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`TODO404`: 투두를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/{todoId}/complete")
  public ResponseEntity<ApiResponse<Void>> completeTodo(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "투두 ID", required = true) @PathVariable Long todoId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    manageTodoService.completeTodo(studyId, todoId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "투두 미완료로 변경", description = "투두를 미완료 상태로 변경합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투두 미완료로 변경 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`TODO4030`: 투두 수정은 작성자만 가능합니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`TODO404`: 투두를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/{todoId}/uncomplete")
  public ResponseEntity<ApiResponse<Void>> uncompleteTodo(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "투두 ID", required = true) @PathVariable Long todoId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    manageTodoService.uncompleteTodo(studyId, todoId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "투두 삭제", description = "투두를 삭제합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "투두 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`TODO403`: 해당 스터디에 속하는 투두가 아닙니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`TODO404`: 투두를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/{todoId}")
  public ResponseEntity<ApiResponse<Void>> deleteTodo(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "투두 ID", required = true) @PathVariable Long todoId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    manageTodoService.deleteTodo(studyId, todoId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
