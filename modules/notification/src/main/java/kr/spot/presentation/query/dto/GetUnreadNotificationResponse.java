package kr.spot.presentation.query.dto;

public record GetUnreadNotificationResponse(
    boolean hasUnreadNotifications
) {

  public static GetUnreadNotificationResponse of(boolean hasUnreadNotifications) {
    return new GetUnreadNotificationResponse(hasUnreadNotifications);
  }
}
