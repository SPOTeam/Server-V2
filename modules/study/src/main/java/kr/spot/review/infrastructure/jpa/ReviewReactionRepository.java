package kr.spot.review.infrastructure.jpa;

import kr.spot.review.domain.associations.ReviewReaction;
import kr.spot.review.domain.enums.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewReactionRepository extends JpaRepository<ReviewReaction, Long> {

  boolean existsByReviewIdAndMemberIdAndReaction(
      Long reviewId, Long memberId, Reaction reaction);

  @Modifying
  @Query(value = """
      DELETE FROM review_reaction
       WHERE review_id = :reviewId
         AND member_id = :memberId
         AND reaction = :reaction
      """, nativeQuery = true)
  int hardDelete(@Param("reviewId") Long reviewId,
      @Param("memberId") Long memberId,
      @Param("reaction") String reaction);
}
