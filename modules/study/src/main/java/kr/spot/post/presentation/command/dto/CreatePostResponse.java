package kr.spot.post.presentation.command.dto;

public record CreatePostResponse(Long postId) {

  public static CreatePostResponse from(long postId) {
    return new CreatePostResponse(postId);
  }
}
