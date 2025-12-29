package kr.spot.todo.presentation.command.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "투두 수정 요청")
public record UpdateTodoRequest(
    @Schema(description = "투두 내용", example = "과제 제출하기")
    String content,

    @Schema(description = "마감일", example = "2025-01-15")
    LocalDate dueDate
) {

}
