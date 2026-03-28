package kr.spot.post.presentation.command.dto;

public record CreateCommentResponse(Long commentId) {

  public static CreateCommentResponse of(Long commentId) {
    return new CreateCommentResponse(commentId);
  }
}
