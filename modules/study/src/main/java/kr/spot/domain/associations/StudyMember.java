package kr.spot.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import kr.spot.domain.enums.Decision;
import kr.spot.domain.enums.StudyMemberStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study_member SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StudyMember extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  private Long memberId;

  private String message;

  @Enumerated(EnumType.STRING)
  private StudyMemberStatus studyMemberStatus;

  public static StudyMember create(Long id, Long studyId, Long memberId) {
    return new StudyMember(id, studyId, memberId, null, StudyMemberStatus.OWNER);
  }

  public static StudyMember apply(Long id, Long studyId, Long memberId, String message) {
    return new StudyMember(id, studyId, memberId, message, StudyMemberStatus.APPLIED);
  }

  public void decide(Decision decision) {
    if (decision == Decision.APPROVE) {
      this.studyMemberStatus = StudyMemberStatus.AWAITING_SELF_APPROVAL;
    } else if (decision == Decision.REJECT) {
      this.studyMemberStatus = StudyMemberStatus.REJECTED;
    }
  }

  public void decideFinal(Decision decision) {
    if (decision == Decision.APPROVE) {
      this.studyMemberStatus = StudyMemberStatus.APPROVED;
    } else if (decision == Decision.REJECT) {
      this.studyMemberStatus = StudyMemberStatus.SELF_REJECTED;
    }
  }
}
