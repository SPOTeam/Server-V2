package kr.spot.application.oauth.dto;

import kr.spot.domain.enums.LoginType;

public record OAuthProfile(LoginType loginType, String email, String nickname,
                           String profileImageUrl) {

  public static OAuthProfile of(LoginType loginType, String email, String nickname,
      String profileImageUrl) {
    return new OAuthProfile(loginType, email, nickname, profileImageUrl);
  }
}
