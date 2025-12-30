package kr.spot.todo.application.command;

import static kr.spot.todo.common.TodoFixture.CONTENT;
import static kr.spot.todo.common.TodoFixture.DUE_DATE;
import static kr.spot.todo.common.TodoFixture.MEMBER_ID;
import static kr.spot.todo.common.TodoFixture.STUDY_ID;
import static kr.spot.todo.common.TodoFixture.createTodoRequest;
import static kr.spot.todo.common.TodoFixture.todo;
import static kr.spot.todo.common.TodoFixture.updateTodoRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.todo.domain.Todo;
import kr.spot.todo.infrastructure.jpa.TodoRepository;
import kr.spot.todo.presentation.command.dto.CreateTodoRequest;
import kr.spot.todo.presentation.command.dto.UpdateTodoRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageTodoServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  TodoRepository todoRepository;

  @Captor
  ArgumentCaptor<Todo> todoCaptor;

  ManageTodoService manageTodoService;

  @BeforeEach
  void setUp() {
    manageTodoService = new ManageTodoService(idGenerator, todoRepository);
  }

  @Nested
  @DisplayName("투두 생성 (createTodo)")
  class CreateTodo {

    @Test
    @DisplayName("투두를 정상적으로 생성할 수 있다")
    void should_create_todo_successfully() {
      // given
      Long generatedId = 1L;
      CreateTodoRequest request = createTodoRequest();

      when(idGenerator.nextId()).thenReturn(generatedId);
      when(todoRepository.save(any(Todo.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageTodoService.createTodo(STUDY_ID, MEMBER_ID, request);

      // then
      verify(todoRepository).save(todoCaptor.capture());

      Todo capturedTodo = todoCaptor.getValue();
      assertThat(capturedTodo.getId()).isEqualTo(generatedId);
      assertThat(capturedTodo.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(capturedTodo.getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(capturedTodo.getContent()).isEqualTo(CONTENT);
      assertThat(capturedTodo.getDueDate()).isEqualTo(DUE_DATE);
      assertThat(capturedTodo.getIsCompleted()).isFalse();
    }
  }

  @Nested
  @DisplayName("투두 수정 (updateTodo)")
  class UpdateTodo {

    @Test
    @DisplayName("투두를 정상적으로 수정할 수 있다")
    void should_update_todo_successfully() {
      // given
      long todoId = 1L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);
      UpdateTodoRequest request = updateTodoRequest();

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when
      manageTodoService.updateTodo(STUDY_ID, todoId, MEMBER_ID, request);

      // then
      verify(todoRepository).getById(todoId);
      assertThat(todo.getContent()).isEqualTo(request.content());
      assertThat(todo.getDueDate()).isEqualTo(request.dueDate());
    }

    @Test
    @DisplayName("다른 스터디의 투두를 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_other_study_todo() {
      // given
      long todoId = 1L;
      long otherStudyId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);
      UpdateTodoRequest request = updateTodoRequest();

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.updateTodo(otherStudyId, todoId, MEMBER_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_by_non_owner() {
      // given
      long todoId = 1L;
      long otherMemberId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);
      UpdateTodoRequest request = updateTodoRequest();

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.updateTodo(STUDY_ID, todoId, otherMemberId, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }

    @Test
    @DisplayName("존재하지 않는 투두를 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_todo_not_found() {
      // given
      long todoId = 999L;
      UpdateTodoRequest request = updateTodoRequest();

      when(todoRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._TODO_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> manageTodoService.updateTodo(STUDY_ID, todoId, MEMBER_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("투두 완료 (completeTodo)")
  class CompleteTodo {

    @Test
    @DisplayName("투두를 정상적으로 완료할 수 있다")
    void should_complete_todo_successfully() {
      // given
      long todoId = 1L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when
      manageTodoService.completeTodo(STUDY_ID, todoId, MEMBER_ID);

      // then
      verify(todoRepository).getById(todoId);
      assertThat(todo.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("다른 스터디의 투두를 완료하려고 하면 예외가 발생한다")
    void should_throw_exception_when_complete_other_study_todo() {
      // given
      long todoId = 1L;
      long otherStudyId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.completeTodo(otherStudyId, todoId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 완료하려고 하면 예외가 발생한다")
    void should_throw_exception_when_complete_by_non_owner() {
      // given
      long todoId = 1L;
      long otherMemberId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.completeTodo(STUDY_ID, todoId, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("투두 미완료 (uncompleteTodo)")
  class UncompleteTodo {

    @Test
    @DisplayName("투두를 정상적으로 미완료로 변경할 수 있다")
    void should_uncomplete_todo_successfully() {
      // given
      long todoId = 1L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);
      todo.complete(STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when
      manageTodoService.uncompleteTodo(STUDY_ID, todoId, MEMBER_ID);

      // then
      verify(todoRepository).getById(todoId);
      assertThat(todo.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("다른 스터디의 투두를 미완료로 변경하려고 하면 예외가 발생한다")
    void should_throw_exception_when_uncomplete_other_study_todo() {
      // given
      long todoId = 1L;
      long otherStudyId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.uncompleteTodo(otherStudyId, todoId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 미완료로 변경하려고 하면 예외가 발생한다")
    void should_throw_exception_when_uncomplete_by_non_owner() {
      // given
      long todoId = 1L;
      long otherMemberId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.uncompleteTodo(STUDY_ID, todoId, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("투두 삭제 (deleteTodo)")
  class DeleteTodo {

    @Test
    @DisplayName("투두를 정상적으로 삭제할 수 있다")
    void should_delete_todo_successfully() {
      // given
      long todoId = 1L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when
      manageTodoService.deleteTodo(STUDY_ID, todoId, MEMBER_ID);

      // then
      verify(todoRepository).getById(todoId);
    }

    @Test
    @DisplayName("다른 스터디의 투두를 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_other_study_todo() {
      // given
      long todoId = 1L;
      long otherStudyId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.deleteTodo(otherStudyId, todoId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_by_non_owner() {
      // given
      long todoId = 1L;
      long otherMemberId = 999L;
      Todo todo = todo(todoId, STUDY_ID, MEMBER_ID);

      when(todoRepository.getById(anyLong())).thenReturn(todo);

      // when & then
      assertThatThrownBy(() -> manageTodoService.deleteTodo(STUDY_ID, todoId, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }

    @Test
    @DisplayName("존재하지 않는 투두를 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_todo_not_found() {
      // given
      long todoId = 999L;

      when(todoRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._TODO_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> manageTodoService.deleteTodo(STUDY_ID, todoId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_NOT_FOUND);
    }
  }
}
