package kr.spot.application.dispatch;

import kr.spot.domain.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 푸시 알림을 실제로 발송하지 않는 더미 구현체. 개발/테스트 환경 또는 FCM 연동 전에 사용합니다.
 */
@Slf4j
@Component
public class NoOpPushNotificationClient implements PushNotificationClient {

  @Override
  public void send(Notification notification) {
    log.info("[NoOp] Push notification would be sent: memberId={}, type={}, title={}",
        notification.getMemberId(),
        notification.getType(),
        notification.getTitle()
    );
  }
}
