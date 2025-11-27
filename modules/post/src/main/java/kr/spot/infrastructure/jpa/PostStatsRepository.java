package kr.spot.infrastructure.jpa;

import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.PostStats;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostStatsRepository extends JpaRepository<PostStats, Long> {

    default PostStats getPostStatsById(long postId) {
        return findById(postId).orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
    }

    @Query("""
            select s.postId
            from PostStats s
            where s.status = 'ACTIVE'
            order by (s.likeCount + s.commentCount + s.viewCount) desc
            limit 3
            """)
    List<Long> findTop3ByTotal();

    @Query("""
            select s.postId
            from PostStats s
            where s.status = 'ACTIVE'
            order by s.likeCount desc
            limit 3
            """)
    List<Long> findTop3ByLikeCount();

    @Query("""
            select s.postId
            from PostStats s
            where s.status = 'ACTIVE'
            order by s.commentCount desc
            limit 3
            """)
    List<Long> findTop3ByCommentCount();

    @Modifying
    @Query("update PostStats s set s.likeCount = s.likeCount + 1 where s.postId = :postId")
    int increaseLike(@Param("postId") long postId);

    @Modifying
    @Query("update PostStats s set s.likeCount = case when s.likeCount > 0 then s.likeCount - 1 else 0 end where s.postId = :postId")
    int decreaseLike(@Param("postId") long postId);

    @Modifying
    @Query("update PostStats s set s.commentCount = s.commentCount + 1 where s.postId = :postId")
    int increaseCommentCount(@Param("postId") long postId);

    @Modifying
    @Query("update PostStats s set s.commentCount = case when s.commentCount > 0 then s.commentCount - 1 else 0 end where s.postId = :postId")
    int decreaseCommentCount(@Param("postId") long postId);

    @Modifying
    @Query("""
                update PostStats ps
                   set ps.viewCount = ps.viewCount + :delta,
                       ps.updatedAt = CURRENT_TIMESTAMP
                 where ps.postId = :postId and ps.status = 'ACTIVE'
            """)
    int increaseViewBy(@Param("postId") long postId, @Param("delta") long delta);

}
