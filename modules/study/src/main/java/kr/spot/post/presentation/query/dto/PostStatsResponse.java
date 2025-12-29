package kr.spot.post.presentation.query.dto;

public record PostStatsResponse(
    Long likeCount,
    Long viewCount,
    Long commentCount
) {

  public static PostStatsResponse from(Long likeCount, Long viewCount, Long commentCount) {
    return new PostStatsResponse(likeCount, viewCount, commentCount);
  }
}
