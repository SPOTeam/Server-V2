package kr.spot.study.domain.enums;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;

public enum StudyMemberStatus {

  OWNER, // 스터디 장
  APPLIED, // 신청 승인 대기
  AWAITING_SELF_APPROVAL, // 본인 승인 대기
  APPROVED, // 신청 승인 완료
  SELF_REJECTED, // 본인 승인 거절
  REJECTED, // 신청 거절
  WITHDRAWN // 스터디 탈퇴
  ;

  public static StudyMemberStatus convert(String status) {
    for (StudyMemberStatus memberStatus : StudyMemberStatus.values()) {
      if (memberStatus.name().equals(status)) {
        return memberStatus;
      }
    }
    throw new GeneralException(ErrorStatus._NO_SUCH_STUDY_MEMBER_STATUS);
  }
}
