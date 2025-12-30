package kr.spot.review.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

  default Review getById(long reviewId) {
    return findById(reviewId).orElseThrow(
        () -> new GeneralException(ErrorStatus._REVIEW_NOT_FOUND));
  }

  default void validateExists(long reviewId) {
    if (!existsById(reviewId)) {
      throw new GeneralException(ErrorStatus._REVIEW_NOT_FOUND);
    }
  }
}
