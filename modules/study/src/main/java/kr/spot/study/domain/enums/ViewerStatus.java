package kr.spot.study.domain.enums;

public enum ViewerStatus {

  NOT_APPLIED,  // 신청하지 않은 사용자
  APPLIED,      // 신청 후 승인 대기 중
  APPROVED,     // 승인 완료된 정식 스터디원
  OWNER         // 스터디장
}
