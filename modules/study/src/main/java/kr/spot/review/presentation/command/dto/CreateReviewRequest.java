package kr.spot.review.presentation.command.dto;

public record CreateReviewRequest(
    String activity,
    String learned,
    String encouragement,
    Boolean isPrivate
) {

  public boolean isPrivateReview() {
    return Boolean.TRUE.equals(isPrivate);
  }
}
