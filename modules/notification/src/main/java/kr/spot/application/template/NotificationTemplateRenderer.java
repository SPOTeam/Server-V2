package kr.spot.application.template;

import java.util.EnumMap;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.NotificationType;
import kr.spot.exception.GeneralException;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateRenderer {

  private static final Map<NotificationType, NotificationTemplate> TEMPLATES;

  static {
    TEMPLATES = new EnumMap<>(NotificationType.class);

    // 스터디 신청 결과
    TEMPLATES.put(NotificationType.STUDY_APPLICATION_APPROVED,
        new NotificationTemplate(
            "${studyName}",
            "스터디 가입이 승인되었습니다! 지금 바로 참여해보세요."
        ));
    TEMPLATES.put(NotificationType.STUDY_APPLICATION_REJECTED,
        new NotificationTemplate(
            "${studyName}",
            "스터디 가입이 거절되었습니다."
        ));

    // 출석 체크
    TEMPLATES.put(NotificationType.ATTENDANCE_STARTED,
        new NotificationTemplate(
            "[${studyName}] 출석 체크",
            "${scheduleName} 출석 체크가 시작되었습니다. 지금 출석해주세요!"
        ));
    TEMPLATES.put(NotificationType.ATTENDANCE_ENDED,
        new NotificationTemplate(
            "[${studyName}] 출석 체크 종료",
            "${scheduleName} 출석 체크가 종료되었습니다."
        ));

    // 스터디 업데이트
    TEMPLATES.put(NotificationType.NOTICE_CREATED,
        new NotificationTemplate(
            "[${studyName}] 새 공지사항",
            "${noticeTitle}"
        ));
    TEMPLATES.put(NotificationType.SCHEDULE_CREATED,
        new NotificationTemplate(
            "[${studyName}] 새 일정",
            "${scheduleName} 일정이 등록되었습니다."
        ));
    TEMPLATES.put(NotificationType.SCHEDULE_UPDATED,
        new NotificationTemplate(
            "[${studyName}] 일정 변경",
            "${scheduleName} 일정이 변경되었습니다."
        ));
    TEMPLATES.put(NotificationType.TODO_COMPLETED,
        new NotificationTemplate(
            "[${studyName}] 할 일 완료",
            "할 일이 완료되었습니다: ${todoTitle}"
        ));

    // 예약 알림
    TEMPLATES.put(NotificationType.SCHEDULE_REMINDER,
        new NotificationTemplate(
            "[${studyName}] 일정 알림",
            "${scheduleName}이(가) 곧 시작됩니다."
        ));
    TEMPLATES.put(NotificationType.HOT_POST_DAILY,
        new NotificationTemplate(
            "오늘의 인기글",
            "${postTitle}"
        ));
  }

  public NotificationContent render(NotificationType type, Map<String, Object> payload) {
    NotificationTemplate template = TEMPLATES.get(type);
    if (template == null) {
      throw new GeneralException(ErrorStatus._NOTIFICATION_TEMPLATE_NOT_FOUND);
    }

    String title = interpolate(template.titleTemplate(), payload);
    String body = interpolate(template.bodyTemplate(), payload);

    return new NotificationContent(title, body);
  }

  private String interpolate(String template, Map<String, Object> payload) {
    String result = template;
    for (Map.Entry<String, Object> entry : payload.entrySet()) {
      String placeholder = "${" + entry.getKey() + "}";
      String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
      result = result.replace(placeholder, value);
    }
    return result;
  }
}
