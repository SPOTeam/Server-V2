package kr.spot.domain.enums;

public enum NotificationStatus {
  PENDING,     // 발송 대기
  PROCESSING,  // 발송 처리 중 (서버가 선점)
  SENT,        // 발송 완료
  FAILED       // 발송 실패 (최대 재시도 초과)
}

