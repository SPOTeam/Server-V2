package kr.spot.application.ports;

import kr.spot.domain.Member;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetMemberInfoService implements GetMemberInfoPort {

  private final MemberRepository memberRepository;

  @Override
  public MemberInfoResponse getMemberInfo(long memberId) {
    Member member = memberRepository.getMemberById(memberId);
    return MemberInfoResponse.of(
        member.getName(),
        member.getProfileImageUrl()
    );
  }
}
