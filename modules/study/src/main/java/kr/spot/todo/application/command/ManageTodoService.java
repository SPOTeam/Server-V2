package kr.spot.todo.application.command;

import kr.spot.IdGenerator;
import kr.spot.todo.domain.Todo;
import kr.spot.todo.infrastructure.jpa.TodoRepository;
import kr.spot.todo.presentation.command.dto.CreateTodoRequest;
import kr.spot.todo.presentation.command.dto.UpdateTodoRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageTodoService {

  private final IdGenerator idGenerator;
  private final TodoRepository todoRepository;

  public void createTodo(long studyId, long memberId, CreateTodoRequest request) {
    Todo todo = Todo.of(
        idGenerator.nextId(),
        studyId,
        memberId,
        request.dueDate(),
        request.content()
    );
    todoRepository.save(todo);
  }

  public void updateTodo(long studyId, long todoId, long memberId, UpdateTodoRequest request) {
    Todo todo = todoRepository.getById(todoId);
    todo.update(studyId, request.content(), request.dueDate(), memberId);
  }

  public void completeTodo(long studyId, long todoId, long memberId) {
    Todo todo = todoRepository.getById(todoId);
    todo.complete(studyId, memberId);
  }

  public void uncompleteTodo(long studyId, long todoId, long memberId) {
    Todo todo = todoRepository.getById(todoId);
    todo.uncomplete(studyId, memberId);
  }

  public void deleteTodo(long studyId, long todoId, long memberId) {
    Todo todo = todoRepository.getById(todoId);
    todo.delete(studyId, memberId);
  }
}
