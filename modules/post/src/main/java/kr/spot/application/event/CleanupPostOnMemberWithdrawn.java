package kr.spot.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.infrastructure.jpa.CommentRepository;
import kr.spot.infrastructure.jpa.PostLikeRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CleanupPostOnMemberWithdrawn {

  private final PostLikeRepository postLikeRepository;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    long memberId = event.memberId();
    postLikeRepository.deleteAllByMemberId(memberId);
    commentRepository.deleteByWriterInfoWriterId(memberId);
    postRepository.deleteByWriterInfoWriterId(memberId);
  }
}
