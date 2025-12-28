package kr.spot.post.application.command;

import static kr.spot.post.common.CommentFixture.CONTENT;
import static kr.spot.post.common.CommentFixture.POST_ID;
import static kr.spot.post.common.CommentFixture.comment;
import static kr.spot.post.common.CommentFixture.manageCommentRequest;
import static kr.spot.post.common.PostFixture.STUDY_ID;
import static kr.spot.post.common.PostFixture.WRITER_ID;
import static kr.spot.post.common.PostFixture.WRITER_NAME;
import static kr.spot.post.common.PostFixture.WRITER_PROFILE_IMAGE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static kr.spot.post.common.PostFixture.post;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.post.domain.Comment;
import kr.spot.post.domain.Post;
import kr.spot.post.infrastructure.jpa.CommentRepository;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.post.presentation.command.dto.CreateCommentResponse;
import kr.spot.post.presentation.command.dto.ManageCommentRequest;
import kr.spot.study.application.validator.StudyAccessValidator;
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
class ManageCommentServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  GetWriterInfoPort getWriterInfoPort;

  @Mock
  CommentRepository commentRepository;

  @Mock
  PostRepository postRepository;

  @Mock
  PostStatsRepository postStatsRepository;

  @Mock
  StudyAccessValidator studyAccessValidator;

  @Captor
  ArgumentCaptor<Comment> commentCaptor;

  ManageCommentService manageCommentService;

  @BeforeEach
  void setUp() {
    manageCommentService = new ManageCommentService(
        idGenerator, getWriterInfoPort, commentRepository, postRepository, postStatsRepository,
        studyAccessValidator);
  }

  @Nested
  @DisplayName("댓글 생성 (createComment)")
  class CreateComment {

    @Test
    @DisplayName("댓글을 정상적으로 생성할 수 있다")
    void should_create_comment_successfully() {
      // given
      Long generatedId = 1L;
      Post post = post(POST_ID, STUDY_ID, WRITER_ID);
      ManageCommentRequest request = manageCommentRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(
          WRITER_ID, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);

      when(postRepository.getById(anyLong())).thenReturn(post);
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(WRITER_ID)).thenReturn(writerInfoResponse);
      when(commentRepository.save(any(Comment.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      CreateCommentResponse response = manageCommentService.createComment(
          WRITER_ID, POST_ID, STUDY_ID, request);

      // then
      assertThat(response.commentId()).isEqualTo(generatedId);

      verify(commentRepository).save(commentCaptor.capture());
      verify(postStatsRepository).increaseCommentCount(POST_ID);

      Comment capturedComment = commentCaptor.getValue();
      assertThat(capturedComment.getId()).isEqualTo(generatedId);
      assertThat(capturedComment.getPostId()).isEqualTo(POST_ID);
      assertThat(capturedComment.getWriterInfo().getWriterId()).isEqualTo(WRITER_ID);
      assertThat(capturedComment.getContent()).isEqualTo(CONTENT);
    }

    @Test
    @DisplayName("스터디 멤버가 아닌 사람이 댓글을 작성하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_study_member_creates() {
      // given
      long nonMemberId = 999L;
      ManageCommentRequest request = manageCommentRequest();

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, nonMemberId);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.createComment(nonMemberId, POST_ID, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("다른 스터디의 게시글에 댓글을 작성하려고 하면 예외가 발생한다")
    void should_throw_exception_when_post_belongs_to_different_study() {
      // given
      long otherStudyId = 999L;
      Post postInOtherStudy = post(POST_ID, otherStudyId, WRITER_ID);
      ManageCommentRequest request = manageCommentRequest();

      when(postRepository.getById(POST_ID)).thenReturn(postInOtherStudy);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.createComment(WRITER_ID, POST_ID, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }

  @Nested
  @DisplayName("댓글 수정 (updateComment)")
  class UpdateComment {

    @Test
    @DisplayName("댓글을 정상적으로 수정할 수 있다")
    void should_update_comment_successfully() {
      // given
      long commentId = 1L;
      Post post = post(POST_ID, STUDY_ID, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);
      ManageCommentRequest request = manageCommentRequest("수정된 댓글 내용");

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(post);

      // when
      manageCommentService.updateComment(WRITER_ID, commentId, STUDY_ID, request);

      // then
      assertThat(comment.getContent()).isEqualTo("수정된 댓글 내용");
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_updates() {
      // given
      long commentId = 1L;
      long otherMemberId = 999L;
      Post post = post(POST_ID, STUDY_ID, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);
      ManageCommentRequest request = manageCommentRequest();

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.updateComment(otherMemberId, commentId, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("스터디 멤버가 아닌 사람이 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_study_member_updates() {
      // given
      long commentId = 1L;
      long nonMemberId = 999L;
      ManageCommentRequest request = manageCommentRequest();

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, nonMemberId);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.updateComment(nonMemberId, commentId, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("다른 스터디의 게시글 댓글을 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_comment_belongs_to_different_study() {
      // given
      long commentId = 1L;
      long otherStudyId = 999L;
      Post postInOtherStudy = post(POST_ID, otherStudyId, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);
      ManageCommentRequest request = manageCommentRequest();

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(postInOtherStudy);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.updateComment(WRITER_ID, commentId, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 수정하려고 하면 예외가 발생한다")
    void should_throw_exception_when_comment_not_found() {
      // given
      long commentId = 999L;
      ManageCommentRequest request = manageCommentRequest();

      when(commentRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.updateComment(WRITER_ID, commentId, STUDY_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._COMMENT_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("댓글 삭제 (deleteComment)")
  class DeleteComment {

    @Test
    @DisplayName("댓글을 정상적으로 삭제할 수 있다")
    void should_delete_comment_successfully() {
      // given
      long commentId = 1L;
      Post post = post(POST_ID, STUDY_ID, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(post);

      // when
      manageCommentService.deleteComment(WRITER_ID, commentId, STUDY_ID);

      // then
      verify(commentRepository).getById(commentId);
      verify(postStatsRepository).decreaseCommentCount(POST_ID);
    }

    @Test
    @DisplayName("작성자가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_author_deletes() {
      // given
      long commentId = 1L;
      long otherMemberId = 999L;
      Post post = post(POST_ID, STUDY_ID, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(post);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.deleteComment(otherMemberId, commentId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }

    @Test
    @DisplayName("스터디 멤버가 아닌 사람이 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_not_study_member_deletes() {
      // given
      long commentId = 1L;
      long nonMemberId = 999L;

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(STUDY_ID, nonMemberId);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.deleteComment(nonMemberId, commentId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 댓글을 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_comment_not_found() {
      // given
      long commentId = 999L;

      when(commentRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.deleteComment(WRITER_ID, commentId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._COMMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 스터디의 게시글 댓글을 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_comment_belongs_to_different_study() {
      // given
      long commentId = 1L;
      long otherStudyId = 999L;
      Post postInOtherStudy = post(POST_ID, otherStudyId, WRITER_ID);
      Comment comment = comment(commentId, POST_ID, WRITER_ID);

      when(commentRepository.getById(commentId)).thenReturn(comment);
      when(postRepository.getById(POST_ID)).thenReturn(postInOtherStudy);

      // when & then
      assertThatThrownBy(
          () -> manageCommentService.deleteComment(WRITER_ID, commentId, STUDY_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }
  }
}
