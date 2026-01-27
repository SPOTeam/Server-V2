package kr.spot.review.application.query;

import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.spot.review.domain.Review;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.domain.vo.Content;
import kr.spot.review.domain.vo.WriterInfo;
import kr.spot.review.infrastructure.jpa.querydsl.ReviewQueryRepository;
import kr.spot.review.infrastructure.jpa.querydsl.ReviewQueryRepository.ReactionCounts;
import kr.spot.review.presentation.query.dto.GetReviewListResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.ContentResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.ReactionCountResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.ReactionResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.ReviewResponse;
import kr.spot.review.presentation.query.dto.GetReviewListResponse.WriterInfoResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetReviewService {

  private static final String PRIVATE_CONTENT_MESSAGE = "이 글은 스터디원에게만 노출됩니다.";

  private final ReviewQueryRepository reviewQueryRepository;
  private final StudyAccessValidator studyAccessValidator;

  public GetReviewListResponse getReviewList(long studyId, Long viewerId, Long cursor, int size) {
    boolean isStudyMember =
        viewerId != null && studyAccessValidator.isStudyMember(studyId, viewerId);
    List<Review> reviews = reviewQueryRepository.findByStudyIdWithCursor(studyId, cursor,
        size + 1);

    boolean hasNext = reviews.size() > size;
    if (hasNext) {
      reviews = reviews.subList(0, size);
    }

    List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
    Map<Long, ReactionCounts> reactionCountsMap = reviewQueryRepository.findReactionCountsByReviewIds(
        reviewIds);
    Map<Long, Set<Reaction>> memberReactionsMap = reviewQueryRepository.findMemberReactionsByReviewIds(
        viewerId, reviewIds);

    long totalElements = reviewQueryRepository.countByStudyId(studyId);
    Long nextCursor = hasNext && !reviews.isEmpty() ? reviews.getLast().getId()
        : null;

    List<ReviewResponse> reviewResponses = reviews.stream()
        .map(review -> toReviewResponse(review, reactionCountsMap, memberReactionsMap,
            isStudyMember))
        .toList();

    return new GetReviewListResponse(reviewResponses, hasNext, nextCursor, totalElements);
  }

  private ReviewResponse toReviewResponse(Review review,
      Map<Long, ReactionCounts> reactionCountsMap,
      Map<Long, Set<Reaction>> memberReactionsMap,
      boolean isStudyMember) {

    WriterInfo writerInfo = review.getWriterInfo();
    Content content = review.getContent();
    ReactionCounts counts = reactionCountsMap.getOrDefault(review.getId(),
        new ReactionCounts(0, 0, 0, 0));
    Set<Reaction> memberReactions = memberReactionsMap.getOrDefault(review.getId(), Set.of());

    ContentResponse contentResponse = createContentResponse(review, content, isStudyMember);

    return ReviewResponse.from(
        review.getId(),
        WriterInfoResponse.from(writerInfo.getWriterId(), writerInfo.getWriterName(),
            writerInfo.getWriterProfileImageUrl()),
        contentResponse,
        ReactionCountResponse.from(counts.fire(), counts.heart(), counts.star(), counts.smile()),
        ReactionResponse.from(
            memberReactions.contains(Reaction.FIRE),
            memberReactions.contains(Reaction.HEART),
            memberReactions.contains(Reaction.STAR),
            memberReactions.contains(Reaction.SMILE)
        ),
        review.isPrivate(),
        review.getCreatedAt()
    );
  }

  private ContentResponse createContentResponse(Review review, Content content,
      boolean isStudyMember) {
    if (review.isPrivate() && !isStudyMember) {
      return ContentResponse.from(
          PRIVATE_CONTENT_MESSAGE,
          PRIVATE_CONTENT_MESSAGE,
          PRIVATE_CONTENT_MESSAGE,
          null
      );
    }
    return ContentResponse.from(
        content.getActivity(),
        content.getLearned(),
        content.getEncouragement(),
        content.getImageUrl()
    );
  }
}
