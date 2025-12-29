package kr.spot.post.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity(name = "StudyPostLike")
@SQLDelete(sql = "UPDATE study_post_like SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "study_post_like",
    uniqueConstraints = @UniqueConstraint(name = "uk_study_post_member", columnNames = {"post_id",
        "member_id"}))
public class PostLike extends BaseEntity {

  @Id
  private Long id;

  private Long postId;

  private Long memberId;

  public static PostLike of(Long id, Long postId, Long memberId) {
    return new PostLike(id, postId, memberId);
  }
}
