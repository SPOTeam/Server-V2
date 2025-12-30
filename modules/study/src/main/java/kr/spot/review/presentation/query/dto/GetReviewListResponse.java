package kr.spot.review.presentation.query.dto;

import java.util.List;

public record GetReviewListResponse(
    List<ReviewResponse> reviews,
    boolean hasNext,
    Long nextCursor,
    Long totalElements
) {

  public record ReviewResponse(
      Long reviewId,
      WriterInfoResponse writer,
      ContentResponse content,
      ReactionCountResponse reactionCounts,
      ReactionResponse reactions,
      boolean isPrivate
  ) {

    public static ReviewResponse from(
        Long reviewId,
        WriterInfoResponse writer,
        ContentResponse content,
        ReactionCountResponse reactionCounts,
        ReactionResponse reactions,
        boolean isPrivate
    ) {
      return new ReviewResponse(reviewId, writer, content, reactionCounts, reactions, isPrivate);
    }
  }

  public record WriterInfoResponse(
      Long memberId,
      String nickname,
      String profileImageUrl
  ) {

    public static WriterInfoResponse from(Long memberId, String nickname, String profileImageUrl) {
      return new WriterInfoResponse(memberId, nickname, profileImageUrl);
    }
  }

  public record ContentResponse(
      String activity,
      String learned,
      String encouragement,
      String imageUrl
  ) {

    public static ContentResponse from(String activity, String learned, String encouragement,
        String imageUrl) {
      return new ContentResponse(activity, learned, encouragement, imageUrl);
    }
  }

  public record ReactionCountResponse(
      Long fireCount,
      Long heartCount,
      Long starCount,
      Long smileCount
  ) {

    public static ReactionCountResponse from(Long fireCount, Long heartCount, Long starCount,
        Long smileCount) {
      return new ReactionCountResponse(fireCount, heartCount, starCount, smileCount);
    }
  }

  public record ReactionResponse(
      boolean isFired,
      boolean isHearted,
      boolean isStarred,
      boolean isSmiled
  ) {

    public static ReactionResponse from(boolean isFired, boolean isHearted, boolean isStarred,
        boolean isSmiled) {
      return new ReactionResponse(isFired, isHearted, isStarred, isSmiled);
    }
  }
}
