package kr.spot.todo.presentation.command.dto;

public record CreateTodoResponse(Long todoId) {

  public static CreateTodoResponse from(long todoId) {
    return new CreateTodoResponse(todoId);
  }
}
