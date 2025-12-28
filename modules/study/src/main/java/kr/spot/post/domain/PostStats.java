package kr.spot.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity(name = "StudyPostStats")
@SQLDelete(sql = "UPDATE study_post_stats SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostStats extends BaseEntity {

  @Id
  private Long postId;

  private Long viewCount;

  private Long likeCount;

  private Long commentCount;

  public static PostStats of(Long postId) {
    return new PostStats(postId, 0L, 0L, 0L);
  }

  public static PostStats of(Long postId, Long viewCount, Long likeCount, Long commentCount) {
    return new PostStats(postId, viewCount, likeCount, commentCount);
  }
}
