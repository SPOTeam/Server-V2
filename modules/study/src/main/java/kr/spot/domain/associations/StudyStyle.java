package kr.spot.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.domain.enums.Style;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study_style SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyStyle extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  @Enumerated(EnumType.STRING)
  private Style style;

  public static StudyStyle of(Long id, Long studyId, Style style) {
    return new StudyStyle(id, studyId, style);
  }
}
