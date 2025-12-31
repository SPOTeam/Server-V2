package kr.spot.review.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.review.infrastructure.jpa.ReviewReactionRepository;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Order(4)
@Component
@RequiredArgsConstructor
public class CleanupReviewOnMemberWithdrawn {

  private final ReviewReactionRepository reviewReactionRepository;
  private final ReviewRepository reviewRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    long memberId = event.memberId();
    reviewReactionRepository.deleteAllByMemberId(memberId);
    reviewRepository.deleteByWriterInfoWriterId(memberId);
  }
}
