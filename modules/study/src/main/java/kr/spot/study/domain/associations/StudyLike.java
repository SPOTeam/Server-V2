package kr.spot.study.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study_like SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "study_like",
    uniqueConstraints = @UniqueConstraint(name = "uk_study_member", columnNames = {"study_id",
        "member_id"}))
public class StudyLike extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  private Long memberId;

  public static StudyLike of(Long id, Long studyId, Long memberId) {
    return new StudyLike(id, studyId, memberId);
  }
}
