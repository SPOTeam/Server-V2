package kr.spot.application.dispatch;

import kr.spot.domain.Notification;

/**
 * 푸시 알림 발송 클라이언트 인터페이스.
 * FCM, APNs 등의 구현체가 이 인터페이스를 구현합니다.
 */
public interface PushNotificationClient {

  /**
   * 푸시 알림을 발송합니다.
   *
   * @param notification 발송할 알림
   * @throws Exception 발송 실패 시
   */
  void send(Notification notification) throws Exception;
}
