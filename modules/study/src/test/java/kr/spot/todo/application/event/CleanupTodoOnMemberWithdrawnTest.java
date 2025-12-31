package kr.spot.todo.application.event;

import static org.mockito.Mockito.verify;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.todo.infrastructure.jpa.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupTodoOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  TodoRepository todoRepository;

  CleanupTodoOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupTodoOnMemberWithdrawn(todoRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 투두를 삭제한다")
  void should_deleteTodo_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    verify(todoRepository).deleteByMemberId(MEMBER_ID);
  }
}
