package kr.spot.application.token;

import static kr.spot.common.AuthFixture.MEMBER_ID;
import static org.mockito.Mockito.verify;

import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

  @Mock
  RefreshTokenRepository refreshTokenRepository;

  LogoutService service;

  @BeforeEach
  void setUp() {
    service = new LogoutService(refreshTokenRepository);
  }

  @Test
  @DisplayName("로그아웃 시 해당 회원의 리프레시 토큰을 삭제한다")
  void should_deleteRefreshToken_when_logout() {
    // when
    service.logout(MEMBER_ID);

    // then
    verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
  }
}
