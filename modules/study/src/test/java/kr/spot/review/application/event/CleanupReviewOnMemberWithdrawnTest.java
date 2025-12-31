package kr.spot.review.application.event;

import static org.mockito.Mockito.inOrder;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.review.infrastructure.jpa.ReviewReactionRepository;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CleanupReviewOnMemberWithdrawnTest {

  private static final long MEMBER_ID = 1L;

  @Mock
  ReviewReactionRepository reviewReactionRepository;

  @Mock
  ReviewRepository reviewRepository;

  CleanupReviewOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new CleanupReviewOnMemberWithdrawn(reviewReactionRepository, reviewRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 리뷰 반응을 먼저 삭제하고 리뷰를 삭제한다")
  void should_deleteReactionsThenReviews_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    InOrder inOrder = inOrder(reviewReactionRepository, reviewRepository);
    inOrder.verify(reviewReactionRepository).deleteAllByMemberId(MEMBER_ID);
    inOrder.verify(reviewRepository).deleteByWriterInfoWriterId(MEMBER_ID);
  }
}
