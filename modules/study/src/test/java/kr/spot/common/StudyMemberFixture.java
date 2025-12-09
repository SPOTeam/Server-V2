package kr.spot.common;

import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;

public class StudyMemberFixture {

  public static final Long ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final Long MEMBER_ID = 200L;
  public static final Long LEADER_ID = 300L;
  public static final String MESSAGE = "스터디에 참여하고 싶습니다.";

  public static StudyMember owner(Long id, Long studyId, Long memberId) {
    return StudyMember.create(id, studyId, memberId);
  }

  public static StudyMember owner() {
    return owner(ID, STUDY_ID, LEADER_ID);
  }

  public static StudyMember applied(Long id, Long studyId, Long memberId, String message) {
    return StudyMember.apply(id, studyId, memberId, message);
  }

  public static StudyMember applied() {
    return applied(ID, STUDY_ID, MEMBER_ID, MESSAGE);
  }

  public static StudyMember awaitingSelfApproval(Long id, Long studyId, Long memberId) {
    StudyMember studyMember = StudyMember.apply(id, studyId, memberId, MESSAGE);
    studyMember.decide(kr.spot.domain.enums.Decision.APPROVE);
    return studyMember;
  }

  public static StudyMember awaitingSelfApproval() {
    return awaitingSelfApproval(ID, STUDY_ID, MEMBER_ID);
  }
}
