package kr.spot.todo.common;

import java.time.LocalDate;
import kr.spot.todo.domain.Todo;
import kr.spot.todo.presentation.command.dto.CreateTodoRequest;
import kr.spot.todo.presentation.command.dto.UpdateTodoRequest;

public class TodoFixture {

  public static final Long ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final Long MEMBER_ID = 200L;
  public static final LocalDate DUE_DATE = LocalDate.of(2025, 1, 15);
  public static final String CONTENT = "과제 제출하기";

  public static Todo todo() {
    return Todo.of(ID, STUDY_ID, MEMBER_ID, DUE_DATE, CONTENT);
  }

  public static Todo todo(Long id) {
    return Todo.of(id, STUDY_ID, MEMBER_ID, DUE_DATE, CONTENT);
  }

  public static Todo todo(Long id, Long studyId, Long memberId) {
    return Todo.of(id, studyId, memberId, DUE_DATE, CONTENT);
  }

  public static Todo completedTodo() {
    Todo todo = todo();
    todo.complete(STUDY_ID, MEMBER_ID);
    return todo;
  }

  public static CreateTodoRequest createTodoRequest() {
    return new CreateTodoRequest(CONTENT, DUE_DATE);
  }

  public static CreateTodoRequest createTodoRequest(String content, LocalDate dueDate) {
    return new CreateTodoRequest(content, dueDate);
  }

  public static UpdateTodoRequest updateTodoRequest() {
    return new UpdateTodoRequest("수정된 내용", LocalDate.of(2025, 1, 20));
  }

  public static UpdateTodoRequest updateTodoRequest(String content, LocalDate dueDate) {
    return new UpdateTodoRequest(content, dueDate);
  }
}
