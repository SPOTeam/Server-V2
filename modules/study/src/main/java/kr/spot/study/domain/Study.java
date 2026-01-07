package kr.spot.study.domain;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.BaseEntity;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.vo.Fee;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE study SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Study extends BaseEntity {

  public static final int CURRENT_MEMBERS = 1;
  @Id
  private Long id;

  private Long leaderId;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private Integer maxMembers;

  @Column(nullable = false)
  private Integer currentMembers = 1;

  @Embedded
  private Fee fee;

  @Column
  private String imageUrl;

  @Column(length = 1000)
  private String description;

  @Enumerated(EnumType.STRING)
  private RecruitingStatus recruitingStatus;

  @Column(nullable = false)
  private Boolean isOnline = false;

  private Long viewCount;

  private Long likeCount;

  public static Study of(Long id, Long leaderId, String name, Integer maxMembers, Fee fee,
      String description) {
    return of(id, leaderId, name, maxMembers, fee, null, description, false);
  }

  public static Study of(Long id, Long leaderId, String name, Integer maxMembers, Fee fee,
      String imageUrl, String description) {
    return of(id, leaderId, name, maxMembers, fee, imageUrl, description, false);
  }

  public static Study of(Long id, Long leaderId, String name, Integer maxMembers, Fee fee,
      String imageUrl, String description, Boolean isOnline) {
    validateStudyNameIsNotBlank(name);
    validateMaxMembers(maxMembers);
    return new Study(id, leaderId, name, maxMembers, CURRENT_MEMBERS, fee, imageUrl, description,
        RecruitingStatus.RECRUITING, isOnline != null && isOnline, 0L, 0L);
  }

  private static void validateStudyNameIsNotBlank(String name) {
    if (StringUtils.isBlank(name)) {
      throw new GeneralException(ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);
    }
  }

  private static void validateMaxMembers(Integer maxMembers) {
    if (maxMembers != null && maxMembers <= 0) {
      throw new GeneralException(ErrorStatus._MAX_MEMBERS_MUST_BE_POSITIVE);
    }
  }

  public void processApplication(StudyMember application, Long requesterId, Decision decision) {
    validateIsFull();
    validateIsStudyOwner(requesterId);
    validateIsValidStatusToProcessApply(application);
    application.decide(decision);
    increaseMemberCountIfApproved(decision);
  }

  private void validateIsFull() {
    if (this.currentMembers >= this.maxMembers) {
      throw new GeneralException(ErrorStatus._STUDY_IS_FULL);
    }
  }

  private void increaseMemberCountIfApproved(Decision decision) {
    if (decision == Decision.APPROVE) {
      this.currentMembers += 1;
    }
  }

  public void validateIsStudyOwner(Long requesterId) {
    if (!this.leaderId.equals(requesterId)) {
      throw new GeneralException(ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }
  }

  private void validateIsValidStatusToProcessApply(StudyMember application) {
    if (application.getStudyMemberStatus() != StudyMemberStatus.APPLIED) {
      throw new GeneralException(ErrorStatus._NOT_PENDING_APPLICATION);
    }
  }

  public StudyMember receiveApplication(Long id, Long memberId, String message) {
    return StudyMember.apply(id, this.id, memberId, message);
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }
}
