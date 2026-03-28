package kr.spot.todo.application.query;

import java.time.LocalDate;
import java.util.List;
import kr.spot.todo.domain.Todo;
import kr.spot.todo.infrastructure.jpa.querydsl.TodoQueryRepository;
import kr.spot.todo.presentation.query.dto.GetTodoListResponse;
import kr.spot.todo.presentation.query.dto.GetTodoListResponse.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetTodoService {

  private final TodoQueryRepository todoQueryRepository;

  public GetTodoListResponse getTodosByDate(long studyId, long memberId, LocalDate dueDate) {
    List<Todo> todos = todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(
        studyId, memberId, dueDate);

    return toResponse(todos);
  }
  
  private GetTodoListResponse toResponse(List<Todo> todos) {
    List<TodoResponse> pending = todos.stream()
        .filter(todo -> !Boolean.TRUE.equals(todo.getIsCompleted()))
        .map(this::toTodoResponse)
        .toList();

    List<TodoResponse> completed = todos.stream()
        .filter(todo -> Boolean.TRUE.equals(todo.getIsCompleted()))
        .map(this::toTodoResponse)
        .toList();

    return GetTodoListResponse.from(pending, completed);
  }

  private TodoResponse toTodoResponse(Todo todo) {
    return TodoResponse.from(
        todo.getId(),
        todo.getContent(),
        todo.getDueDate(),
        todo.getIsCompleted()
    );
  }
}
