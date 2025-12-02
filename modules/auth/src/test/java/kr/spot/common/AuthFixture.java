package kr.spot.common;

import kr.spot.domain.RefreshToken;
import kr.spot.presentation.command.dto.TokenDTO;

public class AuthFixture {

  public static final Long MEMBER_ID = 42L;
  public static final String OLD_REFRESH = "old.refresh.token";
  public static final String NEW_REFRESH = "new.refresh.token";
  public static final String NEW_ACCESS = "new.access.token";

  public static RefreshToken savedRefreshToken(Long id) {
    return RefreshToken.of(id, MEMBER_ID, OLD_REFRESH);
  }

  public static RefreshToken mismatchedRefreshToken(Long id) {
    return RefreshToken.of(id, MEMBER_ID, "another.refresh.token");
  }

  public static TokenDTO newTokenDTO() {
    return new TokenDTO(NEW_ACCESS, NEW_REFRESH);
  }
}
