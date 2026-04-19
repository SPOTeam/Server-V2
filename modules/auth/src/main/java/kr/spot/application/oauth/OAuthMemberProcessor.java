package kr.spot.application.oauth;

import kr.spot.application.oauth.dto.OAuthProfile;
import kr.spot.application.token.TokenProvider;
import kr.spot.domain.RefreshToken;
import kr.spot.impl.Snowflake;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import kr.spot.ports.EnsureMemberFromOAuthPort;
import kr.spot.ports.dto.EnsureResult;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class OAuthMemberProcessor {

  private final Snowflake snowflake = new Snowflake();
  private final EnsureMemberFromOAuthPort ensureMemberFromOAuthPort;
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;
  
  public TokenDTO processOAuthMember(OAuthProfile oAuthProfile) {
    EnsureResult result = ensureMemberFromOAuthPort.ensure(oAuthProfile.loginType().toString(),
        oAuthProfile.email(),
        oAuthProfile.nickname(),
        oAuthProfile.profileImageUrl());

    TokenDTO tokenDTO = tokenProvider.createToken(result.memberId());
    saveRefreshToken(result.memberId(), tokenDTO);
    return TokenDTO.of(tokenDTO.id(), tokenDTO.accessToken(), tokenDTO.refreshToken(), result.isNew());
  }

  private void saveRefreshToken(long createdMemberId, TokenDTO tokenDTO) {
    RefreshToken refreshToken = RefreshToken.of(snowflake.nextId(), createdMemberId,
        tokenDTO.refreshToken());
    refreshTokenRepository.deleteByMemberId(createdMemberId);
    refreshTokenRepository.save(refreshToken);
  }
}
