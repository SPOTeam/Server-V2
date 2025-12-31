package kr.spot.application.token;

import kr.spot.infrastructure.jpa.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogoutService {

  private final RefreshTokenRepository refreshTokenRepository;

  @Transactional
  public void logout(long memberId) {
    refreshTokenRepository.deleteByMemberId(memberId);
  }
}
