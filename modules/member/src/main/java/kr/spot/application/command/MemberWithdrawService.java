package kr.spot.application.command;

import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Member;
import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.HasActiveStudyAsLeaderPort;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService {

  private final MemberRepository memberRepository;
  private final ApplicationEventPublisher eventPublisher;
  private final HasActiveStudyAsLeaderPort hasActiveStudyAsLeaderPort;

  @Transactional
  public void withdraw(long memberId) {
    validateCanWithdraw(memberId);
    Member member = memberRepository.getMemberById(memberId);
    member.withdraw();
    eventPublisher.publishEvent(new MemberWithdrawnEvent(memberId));
  }

  private void validateCanWithdraw(long memberId) {
    if (hasActiveStudyAsLeaderPort.hasActiveStudyAsLeader(memberId)) {
      throw new GeneralException(ErrorStatus._CANNOT_WITHDRAW_WITH_ACTIVE_STUDY);
    }
  }
}
