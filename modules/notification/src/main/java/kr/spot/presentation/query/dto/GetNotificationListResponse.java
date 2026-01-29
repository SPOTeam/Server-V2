package kr.spot.presentation.query.dto;

import java.time.LocalDateTime;
import java.util.List;
import kr.spot.domain.Notification;

public record GetNotificationListResponse(
    List<NotificationResponse> notifications,
    long totalCount
) {

  public static GetNotificationListResponse from(List<Notification> notifications) {
    List<NotificationResponse> responses = notifications.stream()
        .map(NotificationResponse::from)
        .toList();
    return new GetNotificationListResponse(responses, responses.size());
  }

  public record NotificationResponse(
      Long notificationId,
      String type,
      String title,
      String body,
      String imageUrl,
      String referenceType,
      Long referenceId,
      boolean isRead,
      LocalDateTime createdAt
  ) {

    public static NotificationResponse from(Notification notification) {
      return new NotificationResponse(
          notification.getId(),
          notification.getType().name(),
          notification.getTitle(),
          notification.getBody(),
          notification.getImageUrl(),
          notification.getReferenceType(),
          notification.getReferenceId(),
          notification.isRead(),
          notification.getCreatedAt()
      );
    }
  }
}
