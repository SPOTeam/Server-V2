package kr.spot.todo.domain;

import static kr.spot.todo.common.TodoFixture.CONTENT;
import static kr.spot.todo.common.TodoFixture.DUE_DATE;
import static kr.spot.todo.common.TodoFixture.ID;
import static kr.spot.todo.common.TodoFixture.MEMBER_ID;
import static kr.spot.todo.common.TodoFixture.STUDY_ID;
import static kr.spot.todo.common.TodoFixture.todo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TodoTest {

  @Nested
  @DisplayName("투두 생성 (of)")
  class CreateTodo {

    @Test
    @DisplayName("투두 객체를 정상적으로 생성할 수 있다")
    void should_create_todo_successfully() {
      // when
      Todo todo = Todo.of(ID, STUDY_ID, MEMBER_ID, DUE_DATE, CONTENT);

      // then
      assertThat(todo).isNotNull();
      assertThat(todo.getId()).isEqualTo(ID);
      assertThat(todo.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(todo.getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(todo.getDueDate()).isEqualTo(DUE_DATE);
      assertThat(todo.getContent()).isEqualTo(CONTENT);
      assertThat(todo.getIsCompleted()).isFalse();
    }
  }

  @Nested
  @DisplayName("투두 수정 (update)")
  class UpdateTodo {

    @Test
    @DisplayName("투두 내용과 마감일을 정상적으로 수정할 수 있다")
    void should_update_todo_successfully() {
      // given
      Todo todo = todo();
      String newContent = "수정된 내용";
      LocalDate newDueDate = LocalDate.of(2025, 1, 20);

      // when
      todo.update(STUDY_ID, newContent, newDueDate, MEMBER_ID);

      // then
      assertThat(todo.getContent()).isEqualTo(newContent);
      assertThat(todo.getDueDate()).isEqualTo(newDueDate);
    }

    @Test
    @DisplayName("다른 스터디에서 투두를 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_with_different_study_id() {
      // given
      Todo todo = todo();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.update(otherStudyId, "내용", DUE_DATE, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_by_non_owner() {
      // given
      Todo todo = todo();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.update(STUDY_ID, "내용", DUE_DATE, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("투두 완료 (complete)")
  class CompleteTodo {

    @Test
    @DisplayName("투두를 완료 상태로 변경할 수 있다")
    void should_complete_todo_successfully() {
      // given
      Todo todo = todo();

      // when
      todo.complete(STUDY_ID, MEMBER_ID);

      // then
      assertThat(todo.getIsCompleted()).isTrue();
    }

    @Test
    @DisplayName("다른 스터디에서 투두를 완료하려고 하면 예외가 발생한다")
    void should_throw_exception_when_complete_with_different_study_id() {
      // given
      Todo todo = todo();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.complete(otherStudyId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 완료하려고 하면 예외가 발생한다")
    void should_throw_exception_when_complete_by_non_owner() {
      // given
      Todo todo = todo();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.complete(STUDY_ID, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("투두 미완료 (uncomplete)")
  class UncompleteTodo {

    @Test
    @DisplayName("투두를 미완료 상태로 변경할 수 있다")
    void should_uncomplete_todo_successfully() {
      // given
      Todo todo = todo();
      todo.complete(STUDY_ID, MEMBER_ID);

      // when
      todo.uncomplete(STUDY_ID, MEMBER_ID);

      // then
      assertThat(todo.getIsCompleted()).isFalse();
    }

    @Test
    @DisplayName("다른 스터디에서 투두를 미완료로 변경하려고 하면 예외가 발생한다")
    void should_throw_exception_when_uncomplete_with_different_study_id() {
      // given
      Todo todo = todo();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.uncomplete(otherStudyId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 미완료로 변경하려고 하면 예외가 발생한다")
    void should_throw_exception_when_uncomplete_by_non_owner() {
      // given
      Todo todo = todo();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.uncomplete(STUDY_ID, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("투두 삭제 (delete)")
  class DeleteTodo {

    @Test
    @DisplayName("투두를 정상적으로 삭제할 수 있다")
    void should_delete_todo_successfully() {
      // given
      Todo todo = todo();

      // when & then (예외가 발생하지 않으면 성공)
      todo.delete(STUDY_ID, MEMBER_ID);
    }

    @Test
    @DisplayName("다른 스터디의 투두를 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_with_different_study_id() {
      // given
      Todo todo = todo();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.delete(otherStudyId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._TODO_ACCESS_DENIED);
    }

    @Test
    @DisplayName("투두 작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_by_non_owner() {
      // given
      Todo todo = todo();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> todo.delete(STUDY_ID, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }
}
