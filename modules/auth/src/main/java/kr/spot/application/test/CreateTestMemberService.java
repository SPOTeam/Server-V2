package kr.spot.application.test;

import java.util.UUID;
import kr.spot.domain.RefreshToken;
import kr.spot.impl.Snowflake;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import kr.spot.infrastructure.jwt.JwtTokenProvider;
import kr.spot.ports.EnsureMemberFromOAuthPort;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateTestMemberService {

  private final Snowflake snowflake = new Snowflake();
  private final EnsureMemberFromOAuthPort ensureMemberFromOAuthPort;
  private final JwtTokenProvider jwtTokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public TokenDTO createTestMember() {
    String randomEmail = "test_" + UUID.randomUUID().toString().substring(0, 8) + "@test.com";
    String nickname = "테스트유저";

    long memberId = ensureMemberFromOAuthPort.ensure(
        "KAKAO",
        randomEmail,
        nickname,
        null
    ).memberId();

    TokenDTO tokenDTO = jwtTokenProvider.createTestToken(memberId);
    saveRefreshToken(memberId, tokenDTO);
    return tokenDTO;
  }

  private void saveRefreshToken(long memberId, TokenDTO tokenDTO) {
    RefreshToken refreshToken = RefreshToken.of(snowflake.nextId(), memberId,
        tokenDTO.refreshToken());
    refreshTokenRepository.deleteByMemberId(memberId);
    refreshTokenRepository.save(refreshToken);
  }
}
