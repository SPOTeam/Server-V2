package kr.spot.infrastructure.jpa;

import kr.spot.domain.PostViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostViewHistoryRepository extends JpaRepository<PostViewHistory, Long> {

  @Modifying
  @Query(value = """
      UPDATE post_view_history
      SET last_viewed_at = NOW(6)
      WHERE viewer_id = :viewerId
        AND post_id   = :postId
        AND last_viewed_at < DATE_SUB(NOW(6), INTERVAL :minutes MINUTE)
      """, nativeQuery = true)
  int touchIfExpired(
      @Param("viewerId") long viewerId,
      @Param("postId") long postId,
      @Param("minutes") int minutes
  );

  @Modifying
  @Query(value = """
      INSERT IGNORE INTO post_view_history (id, viewer_id, post_id, last_viewed_at)
      VALUES (:id, :viewerId, :postId, NOW(6))
      """, nativeQuery = true)
  int insertIfAbsent(
      @Param("id") long id,
      @Param("viewerId") long viewerId,
      @Param("postId") long postId
  );
}
