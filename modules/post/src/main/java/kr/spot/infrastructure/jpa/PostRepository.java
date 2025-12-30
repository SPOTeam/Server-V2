package kr.spot.infrastructure.jpa;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Post;
import kr.spot.domain.enums.PostType;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select p from Post p where p.id = :id")
  Optional<Post> findByIdWithLock(long id);

  @Modifying
  @Query("""
        update Post p
           set p.title = :title,
               p.content = :content,
               p.postType = :postType,
               p.updatedAt = CURRENT_TIMESTAMP
         where p.id = :postId
           and p.writerInfo.writerId = :writerId
           and p.status = 'ACTIVE'
      """)
  int updatePost(@Param("postId") Long postId,
      @Param("title") String title,
      @Param("content") String content,
      @Param("postType") PostType postType,
      @Param("writerId") Long writerId);

  @Modifying
  @Query("""
        update Post p
           set p.status = 'INACTIVE',
               p.updatedAt = CURRENT_TIMESTAMP
         where p.id = :postId
           and p.writerInfo.writerId = :writerId
           and p.status = 'ACTIVE'
      """)
  int deletePost(@Param("postId") Long postId,
      @Param("writerId") Long writerId);

  @Query("select p from Post p where p.id in :postIds and p.status = 'ACTIVE'")
  List<Post> getPostsByIds(@Param("postIds") List<Long> postIds);

  default Post getPostByIdWithLock(long id) {
    return findByIdWithLock(id).orElseThrow(
        () -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
  }

  default Post getPostById(long id) {
    return findById(id).orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
  }

  default void validateExists(long postId) {
    if (!existsById(postId)) {
      throw new GeneralException(ErrorStatus._POST_NOT_FOUND);
    }
  }
}
