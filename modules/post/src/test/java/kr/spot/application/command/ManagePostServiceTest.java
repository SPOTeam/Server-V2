package kr.spot.application.command;

import static kr.spot.common.PostFixture.CONTENT;
import static kr.spot.common.PostFixture.OTHER_WRITER_ID;
import static kr.spot.common.PostFixture.POST_ID;
import static kr.spot.common.PostFixture.REPORTER_ID;
import static kr.spot.common.PostFixture.REPORT_REASON;
import static kr.spot.common.PostFixture.TITLE;
import static kr.spot.common.PostFixture.WRITER_ID;
import static kr.spot.common.PostFixture.reportPostRequest;
import static kr.spot.common.PostFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.common.PostFixture;
import kr.spot.domain.Post;
import kr.spot.domain.Report;
import kr.spot.domain.enums.PostType;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PostImageRepository;
import kr.spot.infrastructure.jpa.PostLikeRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.infrastructure.jpa.ReportRepository;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.presentation.command.dto.request.ReportPostRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
  FileStoragePort fileStoragePort;

  @Mock
  PostRepository postRepository;

  @Mock
  PostStatsRepository postStatsRepository;

  @Mock
  PostLikeRepository postLikeRepository;

  @Mock
  PostImageRepository postImageRepository;

  @Mock
  ReportRepository reportRepository;

  @Captor
  ArgumentCaptor<Report> reportCaptor;

  ManagePostService managePostService;
  LikePostService likePostService;

  @BeforeEach
  void setUp() {
    managePostService = new ManagePostService(idGenerator, getWriterInfoPort, fileStoragePort,
        postRepository, postStatsRepository, postImageRepository, reportRepository);
    likePostService = new LikePostService(idGenerator, postLikeRepository, postStatsRepository);
  }

  @Test
  @DisplayName("게시글을 정상적으로 생성할 수 있다.")
  void should_create_post_successfully() {
    // given
    WriterInfo writerInfo = writerInfo();

    // when
    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);

    // then
    assertThat(post).isNotNull();
    assertThat(post.getId()).isEqualTo(POST_ID);
    assertThat(post.getTitle()).isEqualTo(TITLE);
    assertThat(post.getContent()).isEqualTo(CONTENT);
    assertThat(post.getPostType()).isEqualTo(PostType.FREE_TALK);
  }

  @Test
  @Disabled
  @DisplayName("게시글 작성자가 아닌 경우 게시글 수정에 실패한다.")
  void should_fail_to_update_post_when_not_writer() {
    // given
    WriterInfo writerInfo = writerInfo();
    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);

    when(postRepository.getPostByIdWithLock(POST_ID)).thenReturn(post);

    // when & then
    assertThatThrownBy(() ->
        managePostService.updatePost(POST_ID, PostFixture.updatePostRequest(), OTHER_WRITER_ID,
            null)
    ).isInstanceOf(GeneralException.class);
  }

  @Test
  @Disabled
  @DisplayName("게시글 작성자가 아닌 경우 게시글 삭제에 실패한다.")
  void should_fail_to_delete_post_when_not_writer() {
    // given
    WriterInfo writerInfo = writerInfo();
    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);

    when(postRepository.getPostByIdWithLock(POST_ID)).thenReturn(post);

    // when & then
    assertThatThrownBy(() ->
        managePostService.deletePost(POST_ID, OTHER_WRITER_ID)
    ).isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("이미 좋아요를 누른 게시글에 대해 다시 좋아요를 누를 경우 정상적으로 처리된다.")
  void should_process_successfully_when_liking_already_liked_post() {
    // given
    when(postLikeRepository.savePostLike(anyLong(), anyLong(), anyLong()))
        .thenReturn(0);

    // when & then
    likePostService.likePost(POST_ID, WRITER_ID);
  }


  @Test
  @DisplayName("좋아요를 누르지 않은 게시글에 대해 좋아요 취소를 할 경우 정상적으로 처리된다.")
  void should_process_successfully_when_unliking_not_liked_post() {
    // given
    when(postLikeRepository.hardDelete(POST_ID, WRITER_ID)).thenReturn(0);

    // when & then
    likePostService.unlikePost(POST_ID, WRITER_ID);
  }

  @Nested
  @DisplayName("게시글 신고 (reportPost)")
  class ReportPost {

    @Test
    @DisplayName("게시글을 정상적으로 신고할 수 있다")
    void should_report_post_successfully() {
      // given
      long generatedId = 100L;
      ReportPostRequest request = reportPostRequest();

      doNothing().when(postRepository).validateExists(anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(reportRepository.save(any(Report.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      managePostService.reportPost(POST_ID, REPORTER_ID, request);

      // then
      verify(postRepository).validateExists(POST_ID);
      verify(reportRepository).save(reportCaptor.capture());

      Report capturedReport = reportCaptor.getValue();
      assertThat(capturedReport.getId()).isEqualTo(generatedId);
      assertThat(capturedReport.getReportedPostId()).isEqualTo(POST_ID);
      assertThat(capturedReport.getReporterId()).isEqualTo(REPORTER_ID);
      assertThat(capturedReport.getReason()).isEqualTo(REPORT_REASON);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 신고하면 예외가 발생한다")
    void should_throw_exception_when_post_not_found() {
      // given
      long nonExistentPostId = 999L;
      ReportPostRequest request = reportPostRequest();

      doThrow(new GeneralException(ErrorStatus._POST_NOT_FOUND))
          .when(postRepository).validateExists(anyLong());

      // when & then
      assertThatThrownBy(
          () -> managePostService.reportPost(nonExistentPostId, REPORTER_ID, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._POST_NOT_FOUND);

      verify(reportRepository, never()).save(any());
    }

    @Test
    @DisplayName("동일한 게시글을 여러 번 신고할 수 있다")
    void should_allow_multiple_reports_on_same_post() {
      // given
      long generatedId1 = 100L;
      long generatedId2 = 101L;
      ReportPostRequest request = reportPostRequest();

      doNothing().when(postRepository).validateExists(anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId1, generatedId2);
      when(reportRepository.save(any(Report.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      managePostService.reportPost(POST_ID, REPORTER_ID, request);
      managePostService.reportPost(POST_ID, OTHER_WRITER_ID, request);

      // then
      verify(reportRepository, org.mockito.Mockito.times(2)).save(any(Report.class));
    }
  }
}
