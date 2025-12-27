package kr.spot.study.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.study.domain.enums.Category;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study_category SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyCategory extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  @Enumerated(EnumType.STRING)
  private Category category;

  public static StudyCategory of(Long id, Long studyId, Category category) {
    return new StudyCategory(id, studyId, category);
  }
}
