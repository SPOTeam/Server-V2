package kr.spot.application.oauth.strategy;


import kr.spot.application.oauth.dto.OAuthProfile;
import kr.spot.domain.enums.LoginType;

public interface OAuthStrategy {

  LoginType getType();

  String getOauthRedirectURL();

  OAuthProfile getOAuthProfile(String code);

  OAuthProfile getOAuthProfileForClient(String accessToken);
}
