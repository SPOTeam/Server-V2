package kr.spot.domain.associations;

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
@SQLDelete(sql = "UPDATE study_region SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyRegion extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  private String regionCode;

  public static StudyRegion of(Long id, Long studyId, String regionCode) {
    return new StudyRegion(id, studyId, regionCode);
  }
}
