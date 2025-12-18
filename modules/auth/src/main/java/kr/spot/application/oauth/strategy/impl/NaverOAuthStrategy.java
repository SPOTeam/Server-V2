package kr.spot.application.oauth.strategy.impl;


import kr.spot.application.oauth.dto.OAuthProfile;
import kr.spot.application.oauth.strategy.OAuthStrategy;
import kr.spot.domain.enums.LoginType;
import kr.spot.infrastructure.oauth.NaverOauth;
import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NaverOAuthStrategy implements OAuthStrategy {

  private final NaverOauth naverOauth;

  @Override
  public LoginType getType() {
    return LoginType.NAVER;
  }

  @Override
  public String getOauthRedirectURL() {
    return naverOauth.getOauthRedirectURL();
  }

  @Override
  public OAuthProfile getOAuthProfile(String code) {
    NaverOAuthTokenDTO token = naverOauth.requestAccessToken(code);
    return requestOAuthProfile(token.access_token());
  }

  @Override
  public OAuthProfile getOAuthProfileForClient(String accessToken) {
    return requestOAuthProfile(accessToken);
  }

  private OAuthProfile requestOAuthProfile(String token) {
    NaverUser user = naverOauth.requestUserInfo(token);
    return OAuthProfile.of(
        getType(), user.response().email(), user.response().name(),
        user.response().thumbnail_image());
  }
}
