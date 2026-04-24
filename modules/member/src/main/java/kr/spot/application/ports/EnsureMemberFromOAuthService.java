package kr.spot.application.ports;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Member;
import kr.spot.domain.enums.LoginType;
import kr.spot.domain.vo.Email;
import kr.spot.exception.GeneralException;
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
    if (checkIsExistMember(email, loginType)) {
      return EnsureResult.of(findMember(email, loginType).getId(), false);
    }
    Member save = createAndSaveMember(email, nickname, imageUrl, loginType);
    return EnsureResult.of(save.getId(), true);
  }

  private Member createAndSaveMember(String email, String nickname, String imageUrl,
      LoginType loginType) {
    Member member = Member.of(idGenerator.nextId(), Email.of(email), nickname, loginType, imageUrl);
    return memberRepository.save(member);
  }

  private boolean checkIsExistMember(String email, LoginType loginType) {
    return memberRepository.existsByEmailAndLoginType(Email.of(email), loginType);
  }

  private Member findMember(String email, LoginType loginType) {
    return memberRepository.findByEmailAndLoginType(Email.of(email), loginType)
        .orElseThrow(() -> new GeneralException(ErrorStatus._MEMBER_NOT_FOUND));
  }
}
