package kr.spot.domain.vo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kr.spot.domain.enums.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTarget {

  private Long targetMemberId;

  private Long linkStudyId;

  @Enumerated(EnumType.STRING)
  private NotificationType notificationType;

  public static NotificationTarget of(Long targetMemberId, Long linkStudyId, NotificationType notificationType) {
    return new NotificationTarget(targetMemberId, linkStudyId, notificationType);
  }
}