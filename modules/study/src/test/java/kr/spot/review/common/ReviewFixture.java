package kr.spot.review.common;

import java.util.List;
import kr.spot.review.domain.Review;
import kr.spot.review.domain.associations.ReviewReaction;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.domain.vo.Content;
import kr.spot.review.domain.vo.WriterInfo;
import kr.spot.review.presentation.command.dto.CreateReviewRequest;

public class ReviewFixture {

  public static final Long ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final Long MEMBER_ID = 200L;
  public static final Long OTHER_MEMBER_ID = 999L;

  public static final String WRITER_NAME = "홍길동";
  public static final String WRITER_PROFILE_IMAGE_URL = "https://example.com/profile.jpg";

  public static final String ACTIVITY = "오늘은 알고리즘 문제 3개를 풀었습니다.";
  public static final String LEARNED = "다이나믹 프로그래밍의 개념을 이해했습니다.";
  public static final String ENCOURAGEMENT = "오늘도 수고했어!";
  public static final String IMAGE_URL = "https://example.com/review-image.jpg";
  public static final String IMAGE_URL_2 = "https://example.com/review-image-2.jpg";
  public static final String IMAGE_URL_3 = "https://example.com/review-image-3.jpg";

  public static WriterInfo writerInfo() {
    return WriterInfo.of(MEMBER_ID, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);
  }

  public static WriterInfo writerInfo(Long memberId) {
    return WriterInfo.of(memberId, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);
  }

  public static Content content() {
    return Content.of(ACTIVITY, LEARNED, ENCOURAGEMENT, List.of(IMAGE_URL));
  }

  public static Content content(String imageUrl) {
    return Content.of(ACTIVITY, LEARNED, ENCOURAGEMENT, imageUrl == null
        ? List.of()
        : List.of(imageUrl));
  }

  public static Content content(List<String> imageUrls) {
    return Content.of(ACTIVITY, LEARNED, ENCOURAGEMENT, imageUrls);
  }

  public static Review review() {
    return Review.of(ID, STUDY_ID, writerInfo(), content(), false);
  }

  public static Review review(Long id) {
    return Review.of(id, STUDY_ID, writerInfo(), content(), false);
  }

  public static Review review(Long id, Long studyId, Long memberId) {
    return Review.of(id, studyId, writerInfo(memberId), content(), false);
  }

  public static Review privateReview() {
    return Review.of(ID, STUDY_ID, writerInfo(), content(), true);
  }

  public static Review privateReview(Long id, Long studyId, Long memberId) {
    return Review.of(id, studyId, writerInfo(memberId), content(), true);
  }

  public static ReviewReaction reviewReaction(Long id, Long reviewId, Long memberId,
      Reaction reaction) {
    return ReviewReaction.of(id, reviewId, memberId, reaction);
  }

  public static CreateReviewRequest createReviewRequest() {
    return new CreateReviewRequest(ACTIVITY, LEARNED, ENCOURAGEMENT, false);
  }

  public static CreateReviewRequest createPrivateReviewRequest() {
    return new CreateReviewRequest(ACTIVITY, LEARNED, ENCOURAGEMENT, true);
  }
}
