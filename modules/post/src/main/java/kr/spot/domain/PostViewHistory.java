package kr.spot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
    name = "post_view_history",
    uniqueConstraints = @UniqueConstraint(
        name = "ux_post_view_history_viewer_post",
        columnNames = {"viewer_id", "post_id"}
    )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PostViewHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "viewer_id", nullable = false)
  private Long viewerId;

  @Column(name = "post_id", nullable = false)
  private Long postId;

  @Column(name = "last_viewed_at", nullable = false)
  private LocalDateTime lastViewedAt;

  public static PostViewHistory of(Long id, Long viewerId, Long postId) {
    return new PostViewHistory(id, viewerId, postId, LocalDateTime.now());
  }
}