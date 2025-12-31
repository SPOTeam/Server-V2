package kr.spot.study.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.study.infrastructure.jpa.associations.StudyLikeRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Order(1)
@Component
@RequiredArgsConstructor
public class CleanupStudyMembershipOnMemberWithdrawn {

  private final StudyMemberRepository studyMemberRepository;
  private final StudyLikeRepository studyLikeRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    long memberId = event.memberId();
    studyMemberRepository.deleteByMemberId(memberId);
    studyLikeRepository.deleteAllByMemberId(memberId);
  }
}
