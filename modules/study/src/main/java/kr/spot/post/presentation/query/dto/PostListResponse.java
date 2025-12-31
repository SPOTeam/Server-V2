package kr.spot.post.presentation.query.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PostListResponse(
    List<PostItem> posts,
    boolean hasNext,
    Long nextCursor
) {

  public record PostItem(
      Long postId,
      String title,
      String content,
      boolean isPinned,
      boolean isLiked,
      PostStatsResponse stats,
      LocalDateTime createdAt
  ) {

    public static PostItem of(
        Long postId,
        String title,
        String content,
        boolean isPinned,
        boolean isLiked,
        PostStatsResponse stats,
        LocalDateTime createdAt
    ) {
      return new PostItem(postId, title, content, isPinned, isLiked, stats, createdAt);
    }
  }

}
