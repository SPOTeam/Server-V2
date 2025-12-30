package kr.spot.todo.application.query;

import static kr.spot.todo.common.TodoFixture.DUE_DATE;
import static kr.spot.todo.common.TodoFixture.MEMBER_ID;
import static kr.spot.todo.common.TodoFixture.STUDY_ID;
import static kr.spot.todo.common.TodoFixture.completedTodo;
import static kr.spot.todo.common.TodoFixture.todo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import kr.spot.todo.domain.Todo;
import kr.spot.todo.infrastructure.jpa.querydsl.TodoQueryRepository;
import kr.spot.todo.presentation.query.dto.GetTodoListResponse;
import kr.spot.todo.presentation.query.dto.GetTodoListResponse.TodoResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetTodoServiceTest {

  @Mock
  TodoQueryRepository todoQueryRepository;

  GetTodoService getTodoService;

  @BeforeEach
  void setUp() {
    getTodoService = new GetTodoService(todoQueryRepository);
  }

  @Nested
  @DisplayName("날짜별 투두 조회 (getTodosByDate)")
  class GetTodosByDate {

    @Test
    @DisplayName("특정 날짜의 투두 목록을 조회할 수 있다")
    void should_get_todos_by_date_successfully() {
      // given
      Todo pendingTodo = todo(1L);
      Todo completedTodo = completedTodo();

      when(todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(anyLong(), anyLong(), any(LocalDate.class)))
          .thenReturn(List.of(pendingTodo, completedTodo));

      // when
      GetTodoListResponse response = getTodoService.getTodosByDate(STUDY_ID, MEMBER_ID, DUE_DATE);

      // then
      verify(todoQueryRepository).findByStudyIdAndMemberIdAndDueDate(STUDY_ID, MEMBER_ID, DUE_DATE);
      assertThat(response.pending()).hasSize(1);
      assertThat(response.completed()).hasSize(1);
    }

    @Test
    @DisplayName("미완료 투두만 있는 경우 pending 목록만 반환된다")
    void should_return_only_pending_todos_when_no_completed() {
      // given
      Todo pendingTodo1 = todo(1L);
      Todo pendingTodo2 = todo(2L);

      when(todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(anyLong(), anyLong(), any(LocalDate.class)))
          .thenReturn(List.of(pendingTodo1, pendingTodo2));

      // when
      GetTodoListResponse response = getTodoService.getTodosByDate(STUDY_ID, MEMBER_ID, DUE_DATE);

      // then
      assertThat(response.pending()).hasSize(2);
      assertThat(response.completed()).isEmpty();
    }

    @Test
    @DisplayName("완료된 투두만 있는 경우 completed 목록만 반환된다")
    void should_return_only_completed_todos_when_no_pending() {
      // given
      Todo completedTodo1 = todo(1L);
      completedTodo1.complete(STUDY_ID, MEMBER_ID);
      Todo completedTodo2 = todo(2L);
      completedTodo2.complete(STUDY_ID, MEMBER_ID);

      when(todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(anyLong(), anyLong(), any(LocalDate.class)))
          .thenReturn(List.of(completedTodo1, completedTodo2));

      // when
      GetTodoListResponse response = getTodoService.getTodosByDate(STUDY_ID, MEMBER_ID, DUE_DATE);

      // then
      assertThat(response.pending()).isEmpty();
      assertThat(response.completed()).hasSize(2);
    }

    @Test
    @DisplayName("투두가 없는 경우 빈 목록이 반환된다")
    void should_return_empty_lists_when_no_todos() {
      // given
      when(todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(anyLong(), anyLong(), any(LocalDate.class)))
          .thenReturn(Collections.emptyList());

      // when
      GetTodoListResponse response = getTodoService.getTodosByDate(STUDY_ID, MEMBER_ID, DUE_DATE);

      // then
      assertThat(response.pending()).isEmpty();
      assertThat(response.completed()).isEmpty();
    }

    @Test
    @DisplayName("투두 응답에 올바른 정보가 포함된다")
    void should_return_correct_todo_information() {
      // given
      Todo todo = todo(1L);

      when(todoQueryRepository.findByStudyIdAndMemberIdAndDueDate(anyLong(), anyLong(), any(LocalDate.class)))
          .thenReturn(List.of(todo));

      // when
      GetTodoListResponse response = getTodoService.getTodosByDate(STUDY_ID, MEMBER_ID, DUE_DATE);

      // then
      assertThat(response.pending()).hasSize(1);
      TodoResponse todoResponse = response.pending().get(0);
      assertThat(todoResponse.id()).isEqualTo(todo.getId());
      assertThat(todoResponse.content()).isEqualTo(todo.getContent());
      assertThat(todoResponse.dueDate()).isEqualTo(todo.getDueDate());
      assertThat(todoResponse.isCompleted()).isEqualTo(todo.getIsCompleted());
    }
  }
}
