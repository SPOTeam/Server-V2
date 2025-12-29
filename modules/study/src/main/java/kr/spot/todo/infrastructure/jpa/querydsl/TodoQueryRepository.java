package kr.spot.todo.infrastructure.jpa.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import kr.spot.todo.domain.QTodo;
import kr.spot.todo.domain.Todo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TodoQueryRepository {

  private final JPAQueryFactory query;

  public List<Todo> findByStudyIdAndMemberIdAndDueDate(Long studyId, Long memberId,
      LocalDate dueDate) {
    QTodo todo = QTodo.todo;

    return query
        .selectFrom(todo)
        .where(
            todo.studyId.eq(studyId),
            todo.memberId.eq(memberId),
            todo.dueDate.eq(dueDate)
        )
        .orderBy(
            todo.isCompleted.asc(),
            todo.createdAt.desc()
        )
        .fetch();
  }
}
