package kr.spot.todo.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.todo.infrastructure.jpa.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Order(3)
@Component
@RequiredArgsConstructor
public class CleanupTodoOnMemberWithdrawn {

  private final TodoRepository todoRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    todoRepository.deleteByMemberId(event.memberId());
  }
}
