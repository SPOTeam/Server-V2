package kr.spot.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
  // 즉시 알림 - 스터디 신청
  STUDY_APPLICATION_APPROVED("스터디 승인", "STUDY"),
  STUDY_APPLICATION_REJECTED("스터디 거절", "STUDY"),

  // 즉시 알림 - 출석
  ATTENDANCE_STARTED("출석 체크 시작", "SCHEDULE"),
  ATTENDANCE_ENDED("출석 체크 종료", "SCHEDULE"),

  // 즉시 알림 - 스터디 업데이트
  NOTICE_CREATED("공지사항 등록", "NOTICE"),
  SCHEDULE_CREATED("일정 등록", "SCHEDULE"),
  SCHEDULE_UPDATED("일정 변경", "SCHEDULE"),
  TODO_COMPLETED("할 일 완료", "TODO"),

  // 예약 알림
  SCHEDULE_REMINDER("일정 리마인드", "SCHEDULE"),
  HOT_POST_DAILY("오늘의 인기글", "POST");

  private final String description;
  private final String referenceType;
}
