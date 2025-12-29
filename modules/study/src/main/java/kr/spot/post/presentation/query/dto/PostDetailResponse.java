package kr.spot.post.presentation.query.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record PostDetailResponse(
    Long postId,
    String title,
    String content,
    boolean isPinned,
    boolean isOwner,
    boolean isLiked,
    WriterInfoResponse writer,
    PostStatsResponse stats,
    LocalDateTime createdAt,
    List<CommentResponse> comments
) {

  public record CommentResponse(
      Long commentId,
      String content,
      boolean isOwner,
      WriterInfoResponse writer,
      LocalDateTime createdAt
  ) {

    public static CommentResponse of(
        Long commentId,
        String content,
        boolean isOwner,
        WriterInfoResponse writer,
        LocalDateTime createdAt
    ) {
      return new CommentResponse(commentId, content, isOwner, writer, createdAt);
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
