package kr.spot.post.common;

import static kr.spot.post.common.PostFixture.writerInfo;

import kr.spot.post.domain.Comment;
import kr.spot.post.presentation.command.dto.ManageCommentRequest;

public class CommentFixture {

  public static final long COMMENT_ID = 1L;
  public static final long POST_ID = 100L;
  public static final String CONTENT = "테스트 댓글 내용입니다.";

  public static Comment comment() {
    return Comment.of(COMMENT_ID, POST_ID, writerInfo(), CONTENT);
  }

  public static Comment comment(long id, long postId) {
    return Comment.of(id, postId, writerInfo(), CONTENT);
  }

  public static Comment comment(long id, long postId, long writerId) {
    return Comment.of(id, postId, writerInfo(writerId), CONTENT);
  }

  public static ManageCommentRequest manageCommentRequest() {
    return new ManageCommentRequest(CONTENT);
  }

  public static ManageCommentRequest manageCommentRequest(String content) {
    return new ManageCommentRequest(content);
  }
}
