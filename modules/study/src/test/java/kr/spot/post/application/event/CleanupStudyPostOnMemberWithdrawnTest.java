package kr.spot.post.application.event;

import static org.mockito.Mockito.inOrder;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.post.infrastructure.jpa.CommentRepository;
import kr.spot.post.infrastructure.jpa.PostLikeRepository;
import kr.spot.post.infrastructure.jpa.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupStudyPostOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  PostLikeRepository postLikeRepository;

  @Mock
  CommentRepository commentRepository;

  @Mock
  PostRepository postRepository;

  CleanupStudyPostOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupStudyPostOnMemberWithdrawn(postLikeRepository, commentRepository, postRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 좋아요, 댓글, 게시글 순서로 삭제한다")
  void should_deleteLikesThenCommentsThenPosts_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    InOrder inOrder = inOrder(postLikeRepository, commentRepository, postRepository);
    inOrder.verify(postLikeRepository).deleteAllByMemberId(MEMBER_ID);
    inOrder.verify(commentRepository).deleteByWriterInfoWriterId(MEMBER_ID);
    inOrder.verify(postRepository).deleteByWriterInfoWriterId(MEMBER_ID);
  }
}
