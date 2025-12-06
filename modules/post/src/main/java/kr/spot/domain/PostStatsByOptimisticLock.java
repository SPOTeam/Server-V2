package kr.spot.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE post_stats_by_optimistic_lock SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostStatsByOptimisticLock extends BaseEntity {

  @Id
  private Long postId;

  private Long likeCount;

  @Version
  private Long version;

  public static PostStatsByOptimisticLock of(Long postId, Long likeCount) {
    return new PostStatsByOptimisticLock(postId, likeCount, 0L);
  }

  public void incrementLikeCount() {
    this.likeCount++;
  }
}
