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
@SQLDelete(sql = "UPDATE study_member_report SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyMemberReport extends BaseEntity {

  @Id
  private Long id;

  private Long studyMemberId;

  private String reportReason;

  public static StudyMemberReport create(Long id, Long studyMemberId, String reportReason) {
    return new StudyMemberReport(id, studyMemberId, reportReason);
  }

}
