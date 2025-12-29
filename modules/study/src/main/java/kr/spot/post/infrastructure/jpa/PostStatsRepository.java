package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.PostStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository(value = "studyPostStatsRepository")
public interface PostStatsRepository extends JpaRepository<PostStats, Long> {

  @Modifying
  @Query("update StudyPostStats s set s.commentCount = s.commentCount + 1 where s.postId = :postId")
  int increaseCommentCount(@Param("postId") long postId);

  @Modifying
  @Query("update StudyPostStats s set s.commentCount = case when s.commentCount > 0 then s.commentCount - 1 else 0 end where s.postId = :postId")
  int decreaseCommentCount(@Param("postId") long postId);

  @Modifying
  @Query("""
      update StudyPostStats s
         set s.viewCount = s.viewCount + :delta,
             s.updatedAt = CURRENT_TIMESTAMP
       where s.postId = :postId and s.status = 'ACTIVE'
      """)
  int increaseViewBy(@Param("postId") long postId, @Param("delta") long delta);

  @Modifying
  @Query("update StudyPostStats s set s.likeCount = s.likeCount + 1 where s.postId = :postId")
  int increaseLike(@Param("postId") long postId);

  @Modifying
  @Query("update StudyPostStats s set s.likeCount = case when s.likeCount > 0 then s.likeCount - 1 else 0 end where s.postId = :postId")
  int decreaseLike(@Param("postId") long postId);
}
