package kr.spot.presentation.query.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import kr.spot.domain.enums.PostType;
import lombok.Builder;

@Builder
public record PostDetailResponse(
    Long postId,
    String title,
    String content,
    String imageUrl,
    PostType postType,
    boolean isLiked,
    boolean isOwner,
    WriterInfoResponse writer,
    PostStatsResponse stats,
    LocalDateTime createdAt,
    List<CommentResponse> comments,
    int commentCount
) {

  public record CommentResponse(
      Long commentId,
      String content,
      WriterInfoResponse writer,
      LocalDateTime createdAt
  ) {

    public static CommentResponse of(Long commentId, String content, WriterInfoResponse writer,
        LocalDateTime createdAt) {
      return new CommentResponse(commentId, content, writer, createdAt);
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
