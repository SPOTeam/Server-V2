package kr.spot.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.vo.Content;
import kr.spot.domain.vo.NotificationTarget;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE notification SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {

  @Id
  private Long id;

  @Embedded
  private Content content;

  @Embedded
  private NotificationTarget target;

  private Boolean isChecked;

  public static Notification of(Long id, Content content, NotificationTarget target) {
    return new Notification(id, content, target, false);
  }
}
