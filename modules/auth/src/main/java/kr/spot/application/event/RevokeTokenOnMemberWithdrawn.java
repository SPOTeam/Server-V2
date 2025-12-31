package kr.spot.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RevokeTokenOnMemberWithdrawn {

  private final RefreshTokenRepository refreshTokenRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    refreshTokenRepository.deleteByMemberId(event.memberId());
  }
}
