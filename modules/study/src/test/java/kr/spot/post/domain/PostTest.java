package kr.spot.post.domain;

import static kr.spot.post.common.PostFixture.CONTENT;
import static kr.spot.post.common.PostFixture.ID;
import static kr.spot.post.common.PostFixture.IS_PRIVATE;
import static kr.spot.post.common.PostFixture.STUDY_ID;
import static kr.spot.post.common.PostFixture.TITLE;
import static kr.spot.post.common.PostFixture.WRITER_ID;
import static kr.spot.post.common.PostFixture.post;
import static kr.spot.post.common.PostFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PostTest {

  @Nested
  @DisplayName("게시글 생성 (of)")
  class CreatePost {

    @Test
    @DisplayName("게시글 객체를 정상적으로 생성할 수 있다")
    void should_create_post_successfully() {
      // when
      Post post = Post.of(ID, STUDY_ID, writerInfo(), TITLE, CONTENT, IS_PRIVATE);

      // then
      assertThat(post).isNotNull();
      assertThat(post.getId()).isEqualTo(ID);
      assertThat(post.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(post.getWriterInfo().getWriterId()).isEqualTo(WRITER_ID);
      assertThat(post.getTitle()).isEqualTo(TITLE);
      assertThat(post.getContent()).isEqualTo(CONTENT);
      assertThat(post.isPrivate()).isEqualTo(IS_PRIVATE);
      assertThat(post.isPinned()).isFalse();
    }

    @Test
    @DisplayName("비공개 게시글을 생성할 수 있다")
    void should_create_private_post() {
      // when
      Post post = Post.of(ID, STUDY_ID, writerInfo(), TITLE, CONTENT, true);

      // then
      assertThat(post.isPrivate()).isTrue();
    }
  }

  @Nested
  @DisplayName("게시글 수정 (update)")
  class UpdatePost {

    @Test
    @DisplayName("작성자가 게시글을 정상적으로 수정할 수 있다")
    void should_update_post_successfully() {
      // given
      Post post = post();
      String newTitle = "수정된 제목";
      String newContent = "수정된 내용";
      boolean newIsPrivate = true;

      // when
      post.update(newTitle, newContent, newIsPrivate, WRITER_ID, STUDY_ID);

      // then
      assertThat(post.getTitle()).isEqualTo(newTitle);
      assertThat(post.getContent()).isEqualTo(newContent);
      assertThat(post.isPrivate()).isEqualTo(newIsPrivate);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_updates() {
      // given
      Post post = post();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(
          () -> post.update("새 제목", "새 내용", false, otherMemberId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("다른 스터디에서 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_from_different_study() {
      // given
      Post post = post();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(
          () -> post.update("새 제목", "새 내용", false, WRITER_ID, otherStudyId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("게시글 삭제 (delete)")
  class DeletePost {

    @Test
    @DisplayName("작성자가 게시글을 정상적으로 삭제할 수 있다")
    void should_delete_post_successfully() {
      // given
      Post post = post();

      // when & then (예외가 발생하지 않으면 성공)
      post.delete(WRITER_ID, STUDY_ID);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_deletes() {
      // given
      Post post = post();
      long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> post.delete(otherMemberId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("다른 스터디에서 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_from_different_study() {
      // given
      Post post = post();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> post.delete(WRITER_ID, otherStudyId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("게시글 핀 (pin)")
  class PinPost {

    @Test
    @DisplayName("게시글을 정상적으로 핀할 수 있다")
    void should_pin_post_successfully() {
      // given
      Post post = post();
      assertThat(post.isPinned()).isFalse();

      // when
      post.pin(STUDY_ID);

      // then
      assertThat(post.isPinned()).isTrue();
      assertThat(post.getPinnedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 스터디에서 핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_pin_from_different_study() {
      // given
      Post post = post();
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> post.pin(otherStudyId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("게시글 언핀 (unpin)")
  class UnpinPost {

    @Test
    @DisplayName("핀된 게시글을 정상적으로 언핀할 수 있다")
    void should_unpin_post_successfully() {
      // given
      Post post = post();
      post.pin(STUDY_ID);
      assertThat(post.isPinned()).isTrue();

      // when
      post.unpin(STUDY_ID);

      // then
      assertThat(post.isPinned()).isFalse();
      assertThat(post.getPinnedAt()).isNull();
    }

    @Test
    @DisplayName("다른 스터디에서 언핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_unpin_from_different_study() {
      // given
      Post post = post();
      post.pin(STUDY_ID);
      long otherStudyId = 999L;

      // when & then
      assertThatThrownBy(() -> post.unpin(otherStudyId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("핀 상태 확인 (isPinned)")
  class IsPinned {

    @Test
    @DisplayName("핀되지 않은 게시글은 false를 반환한다")
    void should_return_false_when_not_pinned() {
      // given
      Post post = post();

      // when & then
      assertThat(post.isPinned()).isFalse();
    }

    @Test
    @DisplayName("핀된 게시글은 true를 반환한다")
    void should_return_true_when_pinned() {
      // given
      Post post = post();
      post.pin(STUDY_ID);

      // when & then
      assertThat(post.isPinned()).isTrue();
    }
  }
}
