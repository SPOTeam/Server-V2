package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.common.CommentFixture;
import kr.spot.common.WriterInfoFixture;
import kr.spot.domain.Comment;
import kr.spot.domain.enums.Status;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.CommentRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.presentation.command.dto.request.ManageCommentRequest;
import kr.spot.presentation.command.dto.response.CreateCommentResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageCommentServiceTest {

  final Long WRITER_ID = 100L;
  final Long OTHER_ID = 200L;
  final Long POST_ID = 10L;
  final Long COMMENT_ID = 999L;
  final String CONTENT = "hello world";

  @Mock
  IdGenerator idGenerator;
  @Mock
  GetWriterInfoPort getWriterInfoPort;
  @Mock
  CommentRepository commentRepository;
  @Mock
  PostStatsRepository postStatsRepository;
  @InjectMocks
  ManageCommentService service;

  @Test
  @DisplayName("새 댓글 생성 시 댓글 저장 및 포스트 댓글 수 증가가 호출되어야 한다")
  void should_create_comment_and_increase_count() {
    // given
    when(idGenerator.nextId()).thenReturn(COMMENT_ID);
    var writerInfoRes = WriterInfoFixture.response(WRITER_ID, "nick", "img");
    when(getWriterInfoPort.get(WRITER_ID)).thenReturn(writerInfoRes);
    when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> inv.getArgument(0));

    // when
    CreateCommentResponse resp = service.createComment(WRITER_ID, POST_ID,
        new ManageCommentRequest(CONTENT));

    // then
    assertThat(resp).isNotNull();
    assertThat(resp.commentId()).isEqualTo(COMMENT_ID);

    verify(commentRepository, times(1)).save(argThat(c ->
        c.getId().equals(COMMENT_ID)
            && c.getPostId().equals(POST_ID)
            && c.getWriterInfo().getWriterId().equals(WRITER_ID)
            && c.getContent().equals(CONTENT)
    ));
    verify(postStatsRepository, times(1)).increaseCommentCount(POST_ID);
    verifyNoMoreInteractions(postStatsRepository);
  }

  @Test
  @DisplayName("작성자가 본인인 경우 댓글을 수정할 수 있다")
  void should_update_comment_when_owner() {
    // given
    var comment = CommentFixture.of(COMMENT_ID, POST_ID, WRITER_ID, "old");
    when(commentRepository.getById(COMMENT_ID)).thenReturn(comment);

    // when
    service.updateComment(WRITER_ID, COMMENT_ID, new ManageCommentRequest("new-content"));

    // then
    assertThat(comment.getContent()).isEqualTo("new-content");
    verify(commentRepository, times(1)).getById(COMMENT_ID);
    verifyNoInteractions(postStatsRepository);
  }

  @Test
  @DisplayName("작성자가 아니면 댓글 수정 시 예외가 발생해야 한다")
  void should_throw_when_update_comment_by_non_owner() {
    // given
    var comment = CommentFixture.of(COMMENT_ID, POST_ID, WRITER_ID, "old");
    when(commentRepository.getById(COMMENT_ID)).thenReturn(comment);

    // when & then
    assertThatThrownBy(() ->
        service.updateComment(OTHER_ID, COMMENT_ID, new ManageCommentRequest("new-content"))
    ).isInstanceOf(GeneralException.class);

    assertThat(comment.getContent()).isEqualTo("old");
  }

  @Test
  @DisplayName("작성자가 본인인 경우 댓글 삭제 시 포스트 댓글 수가 감소한다")
  void should_delete_comment_and_decrease_count_when_owner() {
    // given
    var comment = CommentFixture.of(COMMENT_ID, POST_ID, WRITER_ID, CONTENT);
    when(commentRepository.getById(COMMENT_ID)).thenReturn(comment);

    // when
    service.deleteComment(WRITER_ID, COMMENT_ID);

    // then
    assertThat(comment.getStatus()).isEqualTo(Status.INACTIVE);
    verify(postStatsRepository, times(1)).decreaseCommentCount(POST_ID);
  }

  @Test
  @DisplayName("작성자가 아니면 댓글 삭제 시 예외가 발생하고 카운트는 감소하지 않는다")
  void should_not_decrease_count_when_delete_by_non_owner() {
    // given
    var comment = CommentFixture.of(COMMENT_ID, POST_ID, WRITER_ID, CONTENT);
    when(commentRepository.getById(COMMENT_ID)).thenReturn(comment);

    // when & then
    assertThatThrownBy(() ->
        service.deleteComment(OTHER_ID, COMMENT_ID)
    ).isInstanceOf(GeneralException.class);

    verify(postStatsRepository, never()).decreaseCommentCount(anyLong());
    assertThat(comment.getStatus()).isEqualTo(Status.ACTIVE);
  }
}
