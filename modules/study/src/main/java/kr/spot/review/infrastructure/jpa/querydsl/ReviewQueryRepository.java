package kr.spot.review.infrastructure.jpa.querydsl;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.spot.review.domain.QReview;
import kr.spot.review.domain.Review;
import kr.spot.review.domain.associations.QReviewReaction;
import kr.spot.review.domain.enums.Reaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

  private final JPAQueryFactory query;

  private final QReview review = QReview.review;
  private final QReviewReaction reviewReaction = QReviewReaction.reviewReaction;

  public List<Review> findByStudyIdWithCursor(long studyId, Long cursor, int limit) {
    return query
        .selectFrom(review)
        .where(
            review.studyId.eq(studyId),
            ltCursor(cursor)
        )
        .orderBy(review.id.desc())
        .limit(limit)
        .fetch();
  }

  private BooleanExpression ltCursor(Long cursor) {
    return (cursor == null) ? null : review.id.lt(cursor);
  }

  public long countByStudyId(long studyId) {
    Long count = query
        .select(review.count())
        .from(review)
        .where(review.studyId.eq(studyId))
        .fetchOne();
    return count != null ? count : 0L;
  }

  public Map<Long, ReactionCounts> findReactionCountsByReviewIds(Collection<Long> reviewIds) {
    if (reviewIds.isEmpty()) {
      return Map.of();
    }

    List<ReactionCountRow> rows = query
        .select(Projections.constructor(ReactionCountRow.class,
            reviewReaction.reviewId,
            reviewReaction.reaction,
            reviewReaction.count()))
        .from(reviewReaction)
        .where(reviewReaction.reviewId.in(reviewIds))
        .groupBy(reviewReaction.reviewId, reviewReaction.reaction)
        .fetch();

    return rows.stream()
        .collect(Collectors.groupingBy(
            ReactionCountRow::reviewId,
            Collectors.collectingAndThen(
                Collectors.toList(),
                this::toReactionCounts
            )
        ));
  }

  private ReactionCounts toReactionCounts(List<ReactionCountRow> rows) {
    long fire = 0, heart = 0, star = 0, smile = 0;
    for (ReactionCountRow row : rows) {
      switch (row.reaction()) {
        case FIRE -> fire = row.count();
        case HEART -> heart = row.count();
        case STAR -> star = row.count();
        case SMILE -> smile = row.count();
      }
    }
    return new ReactionCounts(fire, heart, star, smile);
  }

  public Map<Long, Set<Reaction>> findMemberReactionsByReviewIds(
      Long memberId, Collection<Long> reviewIds) {
    if (memberId == null || reviewIds.isEmpty()) {
      return Map.of();
    }

    return query
        .select(reviewReaction.reviewId, reviewReaction.reaction)
        .from(reviewReaction)
        .where(
            reviewReaction.memberId.eq(memberId),
            reviewReaction.reviewId.in(reviewIds)
        )
        .fetch()
        .stream()
        .collect(Collectors.groupingBy(
            tuple -> tuple.get(reviewReaction.reviewId),
            Collectors.mapping(
                tuple -> tuple.get(reviewReaction.reaction),
                Collectors.toSet()
            )
        ));
  }

  public record ReactionCountRow(Long reviewId, Reaction reaction, Long count) {

  }

  public record ReactionCounts(long fire, long heart, long star, long smile) {

  }
}
