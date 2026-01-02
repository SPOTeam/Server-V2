package kr.spot.post.presentation.query.dto;

public record PostStatsResponse(
    long likeCount,
    long viewCount,
    long commentCount
) {

  public static PostStatsResponse from(long likeCount, long viewCount, long commentCount) {
    return new PostStatsResponse(likeCount, viewCount, commentCount);
  }
}
