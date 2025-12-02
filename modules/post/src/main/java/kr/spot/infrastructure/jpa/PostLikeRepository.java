package kr.spot.infrastructure.jpa;

import kr.spot.domain.association.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  @Modifying
  @Query(
      value = """
          INSERT IGNORE INTO post_like(id, post_id, member_id, status, created_at, updated_at)
          VALUES (:id, :postId, :memberId, 'ACTIVE', NOW(), NOW())
          """, nativeQuery = true)
  int savePostLike(@Param("id") long id,
      @Param("postId") long postId,
      @Param("memberId") long memberId);

  @Modifying
  @Query(value = """
      DELETE FROM post_like
       WHERE post_id = :postId
         AND member_id = :memberId
      """, nativeQuery = true)
  int hardDelete(@Param("postId") Long postId,
      @Param("memberId") Long memberId);
}
