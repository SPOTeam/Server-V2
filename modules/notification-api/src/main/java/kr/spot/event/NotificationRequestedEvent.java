package kr.spot.event;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 알림 요청 이벤트.
 * 각 도메인 서비스에서 이 이벤트를 발행하면, Notification 도메인에서 처리합니다.
 *
 * @param type          알림 유형 (문자열로 전달, Notification 도메인에서 NotificationType으로 변환)
 * @param payload       알림 생성에 필요한 데이터 (템플릿 렌더링 및 수신자 결정에 사용)
 * @param scheduledAt   발송 예정 시각 (null이면 즉시 발송)
 * @param referenceId   연관 엔티티 ID (딥링크용)
 * @param imageUrl      알림 이미지 URL (optional)
 */
public record NotificationRequestedEvent(
    String type,
    Map<String, Object> payload,
    LocalDateTime scheduledAt,
    Long referenceId,
    String imageUrl
) {

    /**
     * 즉시 발송 알림 생성
     */
    public static NotificationRequestedEvent immediate(
        String type,
        Map<String, Object> payload,
        Long referenceId,
        String imageUrl
    ) {
        return new NotificationRequestedEvent(type, payload, LocalDateTime.now(), referenceId, imageUrl);
    }

    /**
     * 즉시 발송 알림 생성 (이미지 없음)
     */
    public static NotificationRequestedEvent immediate(
        String type,
        Map<String, Object> payload,
        Long referenceId
    ) {
        return immediate(type, payload, referenceId, null);
    }

    /**
     * 예약 발송 알림 생성
     */
    public static NotificationRequestedEvent scheduled(
        String type,
        LocalDateTime scheduledAt,
        Map<String, Object> payload,
        Long referenceId,
        String imageUrl
    ) {
        return new NotificationRequestedEvent(type, payload, scheduledAt, referenceId, imageUrl);
    }

    /**
     * 예약 발송 알림 생성 (이미지 없음)
     */
    public static NotificationRequestedEvent scheduled(
        String type,
        LocalDateTime scheduledAt,
        Map<String, Object> payload,
        Long referenceId
    ) {
        return scheduled(type, scheduledAt, payload, referenceId, null);
    }
}
