package kr.spot.presentation.query.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.spot.domain.enums.PostType;
import lombok.Builder;

@Builder
public record PostListResponse(
    List<PostList> posts,
    boolean hasNext,
    Long nextCursor
) {

  public record PostList(
      Long postId,
      String title,
      String content,
      PostType postType,
      PostStatsResponse stats,
      LocalDateTime createdAt,
      Boolean isLiked
  ) {

    public static PostList of(Long postId, String title, String content, PostType postType,
        PostStatsResponse stats, LocalDateTime createdAt,
        Boolean isLiked) {
      return new PostList(postId, title, content, postType, stats, createdAt, isLiked);
    }
  }
}
