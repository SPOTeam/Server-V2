package kr.spot.presentation.query.dto.response;

public record PostStatsResponse(
    Long likeCount,
    Long viewCount,
    Long commentCount
) {

  public static PostStatsResponse from(Long likeCount, Long viewCount, Long commentCount) {
    return new PostStatsResponse(likeCount, viewCount, commentCount);
  }
}
