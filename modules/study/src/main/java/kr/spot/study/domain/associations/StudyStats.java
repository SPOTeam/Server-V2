package kr.spot.study.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study_stats SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyStats extends BaseEntity {

  @Id
  private Long studyId;

  private Long viewCount;

  private Long likeCount;

  private Long commentCount;

  public static StudyStats of(Long studyId) {
    return new StudyStats(studyId, 0L, 0L, 0L);
  }
}
