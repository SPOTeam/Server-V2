package kr.spot.todo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDate;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.BaseEntity;
import kr.spot.exception.GeneralException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE todo SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Todo extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  private Long memberId;

  @Column(nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false)
  private String content;

  private Boolean isCompleted;

  public static Todo of(Long id, Long studyId, Long memberId, LocalDate dueDate, String content) {
    return new Todo(id, studyId, memberId, dueDate, content, false);
  }

  public void update(long studyId, String content, LocalDate dueDate, long memberId) {
    validateAccess(studyId);
    validateOwner(memberId);
    this.content = content;
    this.dueDate = dueDate;
  }

  public void complete(long studyId, long memberId) {
    validateAccess(studyId);
    validateOwner(memberId);
    this.isCompleted = true;
  }

  public void uncomplete(long studyId, long memberId) {
    validateAccess(studyId);
    validateOwner(memberId);
    this.isCompleted = false;
  }

  public void delete(long studyId, long memberId) {
    validateAccess(studyId);
    validateOwner(memberId);
    super.delete();
  }

  private void validateAccess(long studyId) {
    if (this.studyId != studyId) {
      throw new GeneralException(ErrorStatus._TODO_ACCESS_DENIED);
    }
  }

  private void validateOwner(long memberId) {
    if (this.memberId != memberId) {
      throw new GeneralException(ErrorStatus._ONLY_TODO_OWNER_CAN_MODIFY);
    }
  }
}
