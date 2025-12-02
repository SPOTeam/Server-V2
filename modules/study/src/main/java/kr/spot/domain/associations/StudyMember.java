package kr.spot.domain.associations;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
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

  @Enumerated(EnumType.STRING)
  private StudyMemberStatus studyMemberStatus;

  public static StudyMember create(Long studyId, Long memberId) {
    return new StudyMember(null, studyId, memberId, StudyMemberStatus.OWNER);
  }

  public static StudyMember apply(Long studyId, Long memberId) {
    return new StudyMember(null, studyId, memberId, StudyMemberStatus.APPLIED);
  }

  public void approve() {
    this.studyMemberStatus = StudyMemberStatus.APPROVED;
  }
}
