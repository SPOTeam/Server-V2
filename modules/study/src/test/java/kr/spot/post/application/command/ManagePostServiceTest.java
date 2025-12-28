package kr.spot.post.application.command;

import static kr.spot.post.common.PostFixture.CONTENT;
import static kr.spot.post.common.PostFixture.IS_PRIVATE;
import static kr.spot.post.common.PostFixture.STUDY_ID;
import static kr.spot.post.common.PostFixture.TITLE;
import static kr.spot.post.common.PostFixture.WRITER_ID;
import static kr.spot.post.common.PostFixture.WRITER_NAME;
import static kr.spot.post.common.PostFixture.WRITER_PROFILE_IMAGE_URL;
import static kr.spot.post.common.PostFixture.managePostRequest;
import static kr.spot.post.common.PostFixture.post;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.post.presentation.command.dto.ManagePostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManagePostServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  GetWriterInfoPort getWriterInfoPort;

  @Mock
  PostRepository postRepository;

  @Mock
  PostStatsRepository postStatsRepository;

  @Captor
  ArgumentCaptor<Post> postCaptor;

  @Captor
  ArgumentCaptor<PostStats> postStatsCaptor;

  ManagePostService managePostService;

  @BeforeEach
  void setUp() {
    managePostService = new ManagePostService(
        idGenerator, getWriterInfoPort, postRepository, postStatsRepository);
  }

  @Nested
  @DisplayName("게시글 생성 (createPost)")
  class CreatePost {

    @Test
    @DisplayName("게시글을 정상적으로 생성할 수 있다")
    void should_create_post_successfully() {
      // given
      Long generatedId = 1L;
      ManagePostRequest request = managePostRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(
          WRITER_ID, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);

      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(WRITER_ID)).thenReturn(writerInfoResponse);
      when(postRepository.save(any(Post.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      managePostService.createPost(STUDY_ID, WRITER_ID, request);

      // then
      verify(postRepository).save(postCaptor.capture());
      verify(postStatsRepository).save(postStatsCaptor.capture());

      Post capturedPost = postCaptor.getValue();
      assertThat(capturedPost.getId()).isEqualTo(generatedId);
      assertThat(capturedPost.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(capturedPost.getWriterInfo().getWriterId()).isEqualTo(WRITER_ID);
      assertThat(capturedPost.getTitle()).isEqualTo(TITLE);
      assertThat(capturedPost.getContent()).isEqualTo(CONTENT);
      assertThat(capturedPost.isPrivate()).isEqualTo(IS_PRIVATE);

      PostStats capturedPostStats = postStatsCaptor.getValue();
      assertThat(capturedPostStats.getPostId()).isEqualTo(generatedId);
      assertThat(capturedPostStats.getViewCount()).isZero();
      assertThat(capturedPostStats.getLikeCount()).isZero();
      assertThat(capturedPostStats.getCommentCount()).isZero();
    }

    @Test
    @DisplayName("비공개 게시글을 생성할 수 있다")
    void should_create_private_post() {
      // given
      Long generatedId = 1L;
      ManagePostRequest request = managePostRequest("제목", "내용", true);
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(
          WRITER_ID, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);

      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(WRITER_ID)).thenReturn(writerInfoResponse);
      when(postRepository.save(any(Post.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      managePostService.createPost(STUDY_ID, WRITER_ID, request);

      // then
      verify(postRepository).save(postCaptor.capture());

      Post capturedPost = postCaptor.getValue();
      assertThat(capturedPost.isPrivate()).isTrue();
    }
  }

  @Nested
  @DisplayName("게시글 수정 (updatePost)")
  class UpdatePost {

    @Test
    @DisplayName("게시글을 정상적으로 수정할 수 있다")
    void should_update_post_successfully() {
      // given
      long postId = 1L;
      Post post = post(postId, STUDY_ID, WRITER_ID);
      ManagePostRequest request = managePostRequest("수정된 제목", "수정된 내용", true);

      when(postRepository.getById(postId)).thenReturn(post);

      // when
      managePostService.updatePost(STUDY_ID, postId, request, WRITER_ID);

      // then
      assertThat(post.getTitle()).isEqualTo("수정된 제목");
      assertThat(post.getContent()).isEqualTo("수정된 내용");
      assertThat(post.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_updates() {
      // given
      long postId = 1L;
      long otherMemberId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);
      ManagePostRequest request = managePostRequest();

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> managePostService.updatePost(STUDY_ID, postId, request, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("다른 스터디에서 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_update_from_different_study() {
      // given
      long postId = 1L;
      long otherStudyId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);
      ManagePostRequest request = managePostRequest();

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> managePostService.updatePost(otherStudyId, postId, request, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_post_not_found() {
      // given
      long postId = 999L;
      ManagePostRequest request = managePostRequest();

      when(postRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._POST_NOT_FOUND));

      // when & then
      assertThatThrownBy(
          () -> managePostService.updatePost(STUDY_ID, postId, request, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._POST_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("게시글 삭제 (deletePost)")
  class DeletePost {

    @Test
    @DisplayName("게시글을 정상적으로 삭제할 수 있다")
    void should_delete_post_successfully() {
      // given
      long postId = 1L;
      Post post = post(postId, STUDY_ID, WRITER_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when
      managePostService.deletePost(STUDY_ID, postId, WRITER_ID);

      // then
      verify(postRepository).getById(postId);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_deletes() {
      // given
      long postId = 1L;
      long otherMemberId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> managePostService.deletePost(STUDY_ID, postId, otherMemberId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("다른 스터디에서 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_delete_from_different_study() {
      // given
      long postId = 1L;
      long otherStudyId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> managePostService.deletePost(otherStudyId, postId, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("게시글 핀 (pinPost)")
  class PinPost {

    @Test
    @DisplayName("게시글을 정상적으로 핀할 수 있다")
    void should_pin_post_successfully() {
      // given
      long postId = 1L;
      Post post = post(postId, STUDY_ID, WRITER_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when
      managePostService.pinPost(STUDY_ID, postId);

      // then
      assertThat(post.isPinned()).isTrue();
      assertThat(post.getPinnedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 스터디에서 핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_pin_from_different_study() {
      // given
      long postId = 1L;
      long otherStudyId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(() -> managePostService.pinPost(otherStudyId, postId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_post_not_found() {
      // given
      long postId = 999L;

      when(postRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._POST_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> managePostService.pinPost(STUDY_ID, postId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._POST_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("게시글 언핀 (unpinPost)")
  class UnpinPost {

    @Test
    @DisplayName("핀된 게시글을 정상적으로 언핀할 수 있다")
    void should_unpin_post_successfully() {
      // given
      long postId = 1L;
      Post post = post(postId, STUDY_ID, WRITER_ID);
      post.pin(STUDY_ID);
      assertThat(post.isPinned()).isTrue();

      when(postRepository.getById(postId)).thenReturn(post);

      // when
      managePostService.unpinPost(STUDY_ID, postId);

      // then
      assertThat(post.isPinned()).isFalse();
      assertThat(post.getPinnedAt()).isNull();
    }

    @Test
    @DisplayName("다른 스터디에서 언핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_unpin_from_different_study() {
      // given
      long postId = 1L;
      long otherStudyId = 999L;
      Post post = post(postId, STUDY_ID, WRITER_ID);
      post.pin(STUDY_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(() -> managePostService.unpinPost(otherStudyId, postId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 언핀하려고 하면 예외가 발생한다")
    void should_throw_exception_when_post_not_found() {
      // given
      long postId = 999L;

      when(postRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._POST_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> managePostService.unpinPost(STUDY_ID, postId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._POST_NOT_FOUND);
    }
  }
}
