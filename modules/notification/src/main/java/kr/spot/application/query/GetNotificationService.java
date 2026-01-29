package kr.spot.application.query;

import java.util.List;
import kr.spot.domain.Notification;
import kr.spot.infrastructure.jpa.NotificationRepository;
import kr.spot.presentation.query.dto.GetNotificationListResponse;
import kr.spot.presentation.query.dto.GetUnreadNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetNotificationService {

  private final NotificationRepository notificationRepository;

  public GetNotificationListResponse getMyNotifications(long memberId) {
    List<Notification> notifications = notificationRepository
        .findByMemberIdOrderByCreatedAtDesc(memberId);
    return GetNotificationListResponse.from(notifications);
  }

  public GetUnreadNotificationResponse hasUnreadNotifications(long memberId) {
    boolean hasUnread = notificationRepository.existsByMemberIdAndIsReadFalse(memberId);
    return new GetUnreadNotificationResponse(hasUnread);
  }
}
