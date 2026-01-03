package kr.spot.review.presentation.command.dto;

public record CreateReviewResponse(Long reviewId) {

  public static CreateReviewResponse from(long reviewId) {
    return new CreateReviewResponse(reviewId);
  }
}
