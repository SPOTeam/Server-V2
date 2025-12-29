package kr.spot.todo.presentation.query.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "투두 목록 응답")
public record GetTodoListResponse(
    @Schema(description = "미완료 투두 목록")
    List<TodoResponse> pending,

    @Schema(description = "완료된 투두 목록")
    List<TodoResponse> completed
) {

  public static GetTodoListResponse from(List<TodoResponse> pending, List<TodoResponse> completed) {
    return new GetTodoListResponse(pending, completed);
  }

  @Schema(description = "투두 상세 정보")
  public record TodoResponse(
      @Schema(description = "투두 ID", example = "1234567890")
      Long id,

      @Schema(description = "투두 내용", example = "과제 제출하기")
      String content,

      @Schema(description = "마감일", example = "2025-01-15")
      LocalDate dueDate,

      @Schema(description = "완료 여부", example = "false")
      Boolean isCompleted
  ) {

    public static TodoResponse from(Long id, String content, LocalDate dueDate,
        Boolean isCompleted) {
      return new TodoResponse(id, content, dueDate, isCompleted);
    }
  }
}
