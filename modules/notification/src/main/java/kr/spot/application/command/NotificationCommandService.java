package kr.spot.application.command;

import kr.spot.domain.Notification;
import kr.spot.infrastructure.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationCommandService {

  private final NotificationRepository notificationRepository;

  public void markAllAsRead(long memberId, long notificationId) {
    Notification notification = notificationRepository.getById(notificationId);
    notification.markAsRead(memberId);
  }

}
