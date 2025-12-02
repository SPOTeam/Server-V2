package kr.spot.domain;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE post_view_history SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostViewHistory extends BaseEntity {

  @Id
  Long id;

  Long viewerId;

  Long postId;

  LocalDateTime viewedAt;

  public static PostViewHistory of(Long id, Long viewerId, Long postId) {
    return new PostViewHistory(id, viewerId, postId, LocalDateTime.now());
  }
}
