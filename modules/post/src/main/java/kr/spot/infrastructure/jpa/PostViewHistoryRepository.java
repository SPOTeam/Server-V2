package kr.spot.infrastructure.jpa;

import kr.spot.domain.PostViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostViewHistoryRepository extends JpaRepository<PostViewHistory, Long> {

  @Modifying
  @Query(value = """
      INSERT IGNORE INTO post_view_history(id, viewer_id, post_id, created_at)
      VALUES (:id, :viewerId, :postId, NOW())
      """, nativeQuery = true)
  int insertIgnore(@Param("id") long id,
      @Param("viewerId") long viewerId,
      @Param("postId") long postId);
}
