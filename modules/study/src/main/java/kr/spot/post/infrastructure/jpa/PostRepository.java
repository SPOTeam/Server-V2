package kr.spot.post.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.post.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

  default Post getById(long postId) {
    return findById(postId).orElseThrow(
        () -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
  }
}
