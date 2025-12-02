package kr.spot.infrastructure.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.exception.GeneralException;
import kr.spot.presentation.command.dto.TokenDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  // 공통 상수
  private static final String SECRET = "a".repeat(64); // HS256용 최소 32바이트 이상 필요
  private static final String OTHER_SECRET = "b".repeat(64);
  private static final long ACCESS_MS = 60_000L;   // 1분
  private static final long REFRESH_MS = 600_000L; // 10분
  private static final long MEMBER_ID = 1234L;

  @Test
  @DisplayName("정상 토큰 생성/검증 및 memberId 추출")
  void create_and_validate_and_extract_memberId() {
    // given
    JwtTokenProvider provider = new JwtTokenProvider(SECRET, ACCESS_MS, REFRESH_MS);

    // when
    TokenDTO token = provider.createToken(MEMBER_ID);

    // then
    assertThat(token.accessToken()).isNotBlank();
    assertThat(token.refreshToken()).isNotBlank();

    assertThatCode(() -> provider.validateToken(token.accessToken())).doesNotThrowAnyException();
    assertThat(provider.getMemberIdByToken(token.accessToken())).isEqualTo(MEMBER_ID);
  }

  @Test
  @DisplayName("만료된 토큰은 EXPIRED_JWT 예외")
  void expired_token_throws_expired_exception() {
    // given: 만료 시간을 음수로 줘서 즉시 만료
    JwtTokenProvider provider = new JwtTokenProvider(SECRET, -1L, -1L);

    String expiredAccess = provider.createToken(MEMBER_ID).accessToken();

    // when & then
    assertThatThrownBy(() -> provider.validateToken(expiredAccess))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("서명이 다른 토큰은 INVALID_JWT 예외")
  void invalid_signature_throws_invalid_exception() {
    JwtTokenProvider legit = new JwtTokenProvider(SECRET, ACCESS_MS, REFRESH_MS);
    String token = legit.createToken(MEMBER_ID).accessToken();

    JwtTokenProvider other = new JwtTokenProvider(OTHER_SECRET, ACCESS_MS, REFRESH_MS);

    assertThatThrownBy(() -> other.validateToken(token))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("빈 문자열 토큰은 EMPTY_JWT 예외")
  void empty_token_throws_empty_exception() {
    JwtTokenProvider provider = new JwtTokenProvider(SECRET, ACCESS_MS, REFRESH_MS);

    assertThatThrownBy(() -> provider.validateToken(""))
        .isInstanceOf(GeneralException.class);
  }
}
