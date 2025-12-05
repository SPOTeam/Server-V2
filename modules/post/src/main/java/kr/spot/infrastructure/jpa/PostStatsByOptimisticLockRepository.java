package kr.spot.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.PostStatsByOptimisticLock;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostStatsByOptimisticLockRepository extends
    JpaRepository<PostStatsByOptimisticLock, Long> {

  default PostStatsByOptimisticLock getPostStatsById(long postId) {
    return findById(postId).orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
  }

}
