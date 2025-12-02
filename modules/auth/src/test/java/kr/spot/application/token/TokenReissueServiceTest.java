package kr.spot.application.token;

import static kr.spot.common.AuthFixture.MEMBER_ID;
import static kr.spot.common.AuthFixture.NEW_ACCESS;
import static kr.spot.common.AuthFixture.NEW_REFRESH;
import static kr.spot.common.AuthFixture.OLD_REFRESH;
import static kr.spot.common.AuthFixture.mismatchedRefreshToken;
import static kr.spot.common.AuthFixture.newTokenDTO;
import static kr.spot.common.AuthFixture.savedRefreshToken;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.RefreshToken;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import kr.spot.presentation.command.dto.TokenDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenReissueServiceTest {

  @Mock
  IdGenerator idGenerator;
  @Mock
  TokenProvider tokenProvider;
  @Mock
  RefreshTokenRepository refreshTokenRepository;

  TokenReissueService service;

  @BeforeEach
  void setUp() {
    service = new TokenReissueService(idGenerator, tokenProvider, refreshTokenRepository);
  }

  @Test
  @DisplayName("성공적으로 토큰을 재발급한다")
  void should_reissueTokenSuccessfully() {
    // given
    RefreshToken saved = savedRefreshToken(100L);

    doNothing().when(tokenProvider).validateToken(OLD_REFRESH);
    when(tokenProvider.getMemberIdByToken(OLD_REFRESH)).thenReturn(MEMBER_ID);
    when(refreshTokenRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(saved));
    when(tokenProvider.createToken(MEMBER_ID)).thenReturn(newTokenDTO());
    when(idGenerator.nextId()).thenReturn(999L);

    // when
    TokenDTO result = service.reissueToken(OLD_REFRESH);

    // then
    assertEquals(NEW_ACCESS, result.accessToken());
    assertEquals(NEW_REFRESH, result.refreshToken());
    verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  @DisplayName("토큰 검증에 실패하면 예외를 던진다")
  void should_throwException_when_tokenInvalidOnValidate() {
    // given
    doThrow(new GeneralException(ErrorStatus._INVALID_REFRESH_TOKEN))
        .when(tokenProvider).validateToken(OLD_REFRESH);

    // when & then
    GeneralException ex = assertThrows(GeneralException.class,
        () -> service.reissueToken(OLD_REFRESH));

    assertEquals(ErrorStatus._INVALID_REFRESH_TOKEN, ex.getStatus());
  }

  @Test
  @DisplayName("DB에 리프레시 토큰이 없으면 예외를 던진다")
  void should_throwException_when_refreshTokenNotFoundInDb() {
    // given
    doNothing().when(tokenProvider).validateToken(OLD_REFRESH);
    when(tokenProvider.getMemberIdByToken(OLD_REFRESH)).thenReturn(MEMBER_ID);
    when(refreshTokenRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

    // when & then
    assertThrows(GeneralException.class, () -> service.reissueToken(OLD_REFRESH));
  }

  @Test
  @DisplayName("DB 저장 토큰과 요청 토큰이 다르면 예외를 던진다")
  void should_throwException_when_refreshTokenMismatch() {
    // given
    RefreshToken mismatched = mismatchedRefreshToken(100L);

    doNothing().when(tokenProvider).validateToken(OLD_REFRESH);
    when(tokenProvider.getMemberIdByToken(OLD_REFRESH)).thenReturn(MEMBER_ID);
    when(refreshTokenRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(mismatched));

    // when & then
    assertThrows(GeneralException.class, () -> service.reissueToken(OLD_REFRESH));
  }

  @Nested
  @DisplayName("정책 검증")
  class RotationPolicyTest {

    @Test
    @DisplayName("재발급 시 기존 토큰은 삭제되고 새 토큰만 저장된다")
    void should_deleteOldAndSaveNew_onReissue() {
      // given
      RefreshToken saved = savedRefreshToken(100L);

      doNothing().when(tokenProvider).validateToken(OLD_REFRESH);
      when(tokenProvider.getMemberIdByToken(OLD_REFRESH)).thenReturn(MEMBER_ID);
      when(refreshTokenRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(saved));
      when(tokenProvider.createToken(MEMBER_ID)).thenReturn(newTokenDTO());
      when(idGenerator.nextId()).thenReturn(1234L);

      // when
      service.reissueToken(OLD_REFRESH);

      // then
      InOrder inOrder = inOrder(refreshTokenRepository);
      inOrder.verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
      inOrder.verify(refreshTokenRepository).save(any(RefreshToken.class));
    }
  }
}
