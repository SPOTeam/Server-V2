package kr.spot.application.token;


import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.RefreshToken;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TokenReissueService {

  private final IdGenerator idGenerator;
  private final TokenProvider tokenProvider;
  private final RefreshTokenRepository refreshTokenRepository;

  public TokenDTO reissueToken(String refreshToken) {
    // 리프레시 토큰 검증
    tokenProvider.validateToken(refreshToken);

    // DB에서 리프레시 토큰 조회
    Long memberId = tokenProvider.getMemberIdByToken(refreshToken);
    RefreshToken refreshTokenByMemberId = getRefreshTokenByMemberId(memberId);
    validateIsValidRefreshToken(refreshToken, refreshTokenByMemberId);

    // 새로운 토큰 생성
    return createNewTokens(memberId, refreshTokenByMemberId);
  }

  private void validateIsValidRefreshToken(String refreshToken,
      RefreshToken refreshTokenByMemberId) {
    if (!refreshTokenByMemberId.getToken().equals(refreshToken)) {
      throw new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN);
    }
  }

  private RefreshToken getRefreshTokenByMemberId(Long memberId) {
    return refreshTokenRepository.findByMemberId(memberId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN));
  }

  private TokenDTO createNewTokens(Long memberId, RefreshToken refreshToken) {
    TokenDTO token = tokenProvider.createToken(memberId);
    refreshTokenRepository.deleteByMemberId(refreshToken.getMemberId());
    refreshTokenRepository.save(
        RefreshToken.of(idGenerator.nextId(), memberId, token.refreshToken()));
    return token;
  }
}
