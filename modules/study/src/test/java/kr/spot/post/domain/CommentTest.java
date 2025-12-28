package kr.spot.post.domain;

import static kr.spot.post.common.CommentFixture.COMMENT_ID;
import static kr.spot.post.common.CommentFixture.CONTENT;
import static kr.spot.post.common.CommentFixture.POST_ID;
import static kr.spot.post.common.CommentFixture.comment;
import static kr.spot.post.common.PostFixture.WRITER_ID;
import static kr.spot.post.common.PostFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CommentTest {

  @Nested
  @DisplayName("댓글 생성 (of)")
  class CreateComment {

    @Test
    @DisplayName("댓글 객체를 정상적으로 생성할 수 있다")
    void should_create_comment_successfully() {
      // when
      Comment comment = Comment.of(COMMENT_ID, POST_ID, writerInfo(), CONTENT);

      // then
      assertThat(comment).isNotNull();
      assertThat(comment.getId()).isEqualTo(COMMENT_ID);
      assertThat(comment.getPostId()).isEqualTo(POST_ID);
      assertThat(comment.getWriterInfo().getWriterId()).isEqualTo(WRITER_ID);
      assertThat(comment.getContent()).isEqualTo(CONTENT);
    }
  }

  @Nested
  @DisplayName("댓글 수정 (update)")
  class UpdateComment {

    @Test
    @DisplayName("작성자가 댓글을 정상적으로 수정할 수 있다")
    void should_update_comment_successfully() {
      // given
      Comment comment = comment();
      String newContent = "수정된 댓글 내용";

      // when
      comment.update(WRITER_ID, newContent);

      // then
      assertThat(comment.getContent()).isEqualTo(newContent);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_updates() {
      // given
      Comment comment = comment();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> comment.update(otherMemberId, "새 내용"))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("댓글 삭제 (delete)")
  class DeleteComment {

    @Test
    @DisplayName("작성자가 댓글을 정상적으로 삭제할 수 있다")
    void should_delete_comment_successfully() {
      // given
      Comment comment = comment();

      // when & then (예외가 발생하지 않으면 성공)
      comment.delete(WRITER_ID);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_deletes() {
      // given
      Comment comment = comment();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> comment.delete(otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }
  }
}
