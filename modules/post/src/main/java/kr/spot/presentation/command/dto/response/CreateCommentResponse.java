package kr.spot.presentation.command.dto.response;

public record CreateCommentResponse(
    Long commentId
) {

  public static CreateCommentResponse of(Long commentId) {
    return new CreateCommentResponse(commentId);
  }
}
