package kr.spot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.NotificationStatus;
import kr.spot.domain.enums.NotificationType;
import kr.spot.exception.GeneralException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "notification", indexes = {
    @Index(name = "idx_dispatch", columnList = "dispatchStatus, scheduledAt, pickedBy"),
    @Index(name = "idx_member_created", columnList = "memberId, createdAt"),
    @Index(name = "idx_retry", columnList = "dispatchStatus, nextRetryAt")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

  private static final int DEFAULT_MAX_RETRY = 3;

  @Id
  private Long id;

  // 수신자
  @Column(nullable = false)
  private Long memberId;

  // 알림 타입
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  private NotificationType type;

  // 알림 내용
  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, length = 500)
  private String body;

  @Column(length = 500)
  private String imageUrl;

  // 연관 데이터 (딥링크용)
  @Column(length = 50)
  private String referenceType;

  private Long referenceId;

  // 발송 상태 관리
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private NotificationStatus dispatchStatus;

  @Column(nullable = false)
  private LocalDateTime scheduledAt;

  private LocalDateTime sentAt;

  // 동시성 제어 & 재시도
  @Column(length = 50)
  private String pickedBy;

  private LocalDateTime pickedAt;

  @Column(nullable = false)
  private int retryCount;

  @Column(nullable = false)
  private int maxRetry;

  private LocalDateTime nextRetryAt;

  // 중복 방지
  @Column(unique = true, length = 200)
  private String dedupeKey;

  // 실패 정보
  @Column(length = 500)
  private String lastError;

  // 읽음 여부
  @Column(nullable = false)
  private boolean isRead;

  private Notification(
      Long id,
      Long memberId,
      NotificationType type,
      String title,
      String body,
      String imageUrl,
      String referenceType,
      Long referenceId,
      LocalDateTime scheduledAt,
      String dedupeKey
  ) {
    this.id = id;
    this.memberId = memberId;
    this.type = type;
    this.title = title;
    this.body = body;
    this.imageUrl = imageUrl;
    this.referenceType = referenceType;
    this.referenceId = referenceId;
    this.dispatchStatus = NotificationStatus.PENDING;
    this.scheduledAt = scheduledAt;
    this.retryCount = 0;
    this.maxRetry = DEFAULT_MAX_RETRY;
    this.isRead = false;
  }

  public static Notification create(
      long id,
      long memberId,
      NotificationType type,
      String title,
      String body,
      String imageUrl,
      String referenceType,
      Long referenceId,
      LocalDateTime scheduledAt,
      String dedupeKey
  ) {
    return new Notification(
        id, memberId, type, title, body, imageUrl,
        referenceType, referenceId, scheduledAt, dedupeKey
    );
  }

  public void markAsSent(LocalDateTime now) {
    this.dispatchStatus = NotificationStatus.SENT;
    this.sentAt = now;
    this.pickedBy = null;
    this.pickedAt = null;
  }

  public void markAsFailed(String errorMessage) {
    this.dispatchStatus = NotificationStatus.FAILED;
    this.lastError = errorMessage;
    this.pickedBy = null;
    this.pickedAt = null;
  }

  public void scheduleRetry(LocalDateTime nextRetryAt) {
    this.dispatchStatus = NotificationStatus.PENDING;
    this.retryCount++;
    this.nextRetryAt = nextRetryAt;
    this.pickedBy = null;
    this.pickedAt = null;
  }

  public boolean canRetry() {
    return retryCount < maxRetry;
  }

  public void markAsRead(long memberId) {
    if (memberId != this.memberId) {
      throw new GeneralException(ErrorStatus._NOTIFICATION_ACCESS_DENIED);
    }
    this.isRead = true;
  }
}
