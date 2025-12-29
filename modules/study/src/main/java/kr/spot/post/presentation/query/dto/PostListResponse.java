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
      PostStatsResponse stats,
      WriterInfoResponse writer,
      LocalDateTime createdAt
  ) {

    public static PostItem of(
        Long postId,
        String title,
        String content,
        boolean isPinned,
        PostStatsResponse stats,
        WriterInfoResponse writer,
        LocalDateTime createdAt
    ) {
      return new PostItem(postId, title, content, isPinned, stats, writer, createdAt);
    }
  }

  public record WriterInfoResponse(
      Long writerId,
      String nickname,
      String profileImageUrl
  ) {

    public static WriterInfoResponse of(Long writerId, String nickname, String profileImageUrl) {
      return new WriterInfoResponse(writerId, nickname, profileImageUrl);
    }
  }
}
