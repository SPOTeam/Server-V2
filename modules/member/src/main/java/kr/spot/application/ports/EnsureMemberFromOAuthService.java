package kr.spot.application.ports;

import kr.spot.IdGenerator;
import kr.spot.domain.Member;
import kr.spot.domain.enums.LoginType;
import kr.spot.domain.enums.Status;
import kr.spot.domain.vo.Email;
import kr.spot.infrastructure.jpa.MemberRepository;
import kr.spot.ports.EnsureMemberFromOAuthPort;
import kr.spot.ports.dto.EnsureResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnsureMemberFromOAuthService implements EnsureMemberFromOAuthPort {

  private final IdGenerator idGenerator;
  private final MemberRepository memberRepository;

  @Override
  public EnsureResult ensure(String provider, String email, String nickname, String imageUrl) {
    LoginType loginType = LoginType.valueOf(provider);
    return memberRepository
        .findByEmailAndLoginTypeIncludingInactive(email, loginType.name())
        .map(member -> reuseOrReactivate(member, nickname, imageUrl))
        .orElseGet(() -> createAsNew(email, nickname, imageUrl, loginType));
  }

  private EnsureResult reuseOrReactivate(Member member, String nickname, String imageUrl) {
    if (member.getStatus() == Status.INACTIVE) {
      member.reactivate(nickname, imageUrl);
      return EnsureResult.of(member.getId(), true);
    }
    return EnsureResult.of(member.getId(), false);
  }

  private EnsureResult createAsNew(String email, String nickname, String imageUrl,
      LoginType loginType) {
    Member member = Member.of(idGenerator.nextId(), Email.of(email), nickname, loginType, imageUrl);
    return EnsureResult.of(memberRepository.save(member).getId(), true);
  }
}
