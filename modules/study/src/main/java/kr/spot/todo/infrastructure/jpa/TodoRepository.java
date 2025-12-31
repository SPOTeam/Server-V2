package kr.spot.todo.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.todo.domain.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoRepository extends JpaRepository<Todo, Long> {

  default Todo getById(long todoId) {
    return findById(todoId).orElseThrow(
        () -> new GeneralException(ErrorStatus._TODO_NOT_FOUND));
  }

  void deleteByMemberId(long memberId);
}
