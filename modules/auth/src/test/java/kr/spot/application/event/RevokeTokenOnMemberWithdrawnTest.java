package kr.spot.application.event;

import static kr.spot.common.AuthFixture.MEMBER_ID;
import static org.mockito.Mockito.verify;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RevokeTokenOnMemberWithdrawnTest {

  @Mock
  RefreshTokenRepository refreshTokenRepository;

  RevokeTokenOnMemberWithdrawn handler;

  @BeforeEach
  void setUp() {
    handler = new RevokeTokenOnMemberWithdrawn(refreshTokenRepository);
  }

  @Test
  @DisplayName("회원 탈퇴 시 해당 회원의 리프레시 토큰을 삭제한다")
  void should_deleteRefreshToken_when_memberWithdrawn() {
    // given
    MemberWithdrawnEvent event = new MemberWithdrawnEvent(MEMBER_ID);

    // when
    handler.handle(event);

    // then
    verify(refreshTokenRepository).deleteByMemberId(MEMBER_ID);
  }
}
