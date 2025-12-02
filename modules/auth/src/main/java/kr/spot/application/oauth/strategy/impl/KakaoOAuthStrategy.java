package kr.spot.application.oauth.strategy.impl;


import kr.spot.application.oauth.dto.OAuthProfile;
import kr.spot.application.oauth.strategy.OAuthStrategy;
import kr.spot.domain.enums.LoginType;
import kr.spot.infrastructure.oauth.KaKaoOauth;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuthStrategy implements OAuthStrategy {

  private final KaKaoOauth kaKaoOauth;

  @Override
  public LoginType getType() {
    return LoginType.KAKAO;
  }

  @Override
  public String getOauthRedirectURL() {
    return kaKaoOauth.getOauthRedirectURL();
  }

  @Override
  public OAuthProfile getOAuthProfile(String code) {
    KaKaoOAuthTokenDTO token = kaKaoOauth.requestAccessToken(code);
    log.info(token.access_token());
    return requestOAuthProfile(token.access_token());
  }

  @Override
  public OAuthProfile getOAuthProfileForClient(String accessToken) {
    return requestOAuthProfile(accessToken);
  }

  private OAuthProfile requestOAuthProfile(String accessToken) {
    KaKaoUser user = kaKaoOauth.requestUserInfo(accessToken);
    return OAuthProfile.of(getType(), user.kakao_account().email(), user.properties().nickname(),
        user.properties().profile_image());
  }
}
