package kr.spot.presentation.command.dto.response;

public record CreatePostResponse(Long postId) {

  public static CreatePostResponse from(Long postId) {
    return new CreatePostResponse(postId);
  }
}
