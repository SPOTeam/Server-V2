package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository(value = "studyPostLikeRepository")
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  @Modifying
  @Query(
      value = """
          INSERT IGNORE INTO study_post_like(id, post_id, member_id, status, created_at, updated_at)
          VALUES (:id, :postId, :memberId, 'ACTIVE', NOW(), NOW())
          """, nativeQuery = true)
  int savePostLike(@Param("id") long id,
      @Param("postId") long postId,
      @Param("memberId") long memberId);

  @Modifying
  @Query(value = """
      DELETE FROM study_post_like
       WHERE post_id = :postId
         AND member_id = :memberId
      """, nativeQuery = true)
  int hardDelete(@Param("postId") Long postId,
      @Param("memberId") Long memberId);
}
