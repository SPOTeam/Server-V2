package kr.spot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE post_stats_by_perssimistic_lock SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostStatsByPessimisticLock extends BaseEntity {

  @Id
  private Long postId;

  private Long likeCount;

  public static PostStatsByPessimisticLock of(Long postId, Long likeCount) {
    return new PostStatsByPessimisticLock(postId, likeCount);
  }

  public void incrementLikeCount() {
    this.likeCount++;
  }
}
