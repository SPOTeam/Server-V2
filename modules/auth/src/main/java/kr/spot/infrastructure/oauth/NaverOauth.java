package kr.spot.infrastructure.oauth;

import static kr.spot.infrastructure.constants.AuthConstants.CLIENT_ID;
import static kr.spot.infrastructure.constants.AuthConstants.GRANT_TYPE_AUTHORIZATION_CODE;
import static kr.spot.infrastructure.constants.AuthConstants.KEY_VALUE_DELIMITER;
import static kr.spot.infrastructure.constants.AuthConstants.QUERY_DELIMITER;
import static kr.spot.infrastructure.constants.AuthConstants.QUERY_PREFIX;
import static kr.spot.infrastructure.constants.AuthConstants.REDIRECT_URI;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE_CODE;
import static kr.spot.infrastructure.constants.AuthConstants.STATE;
import static kr.spot.infrastructure.constants.AuthConstants.STATE_STRING;
import static kr.spot.infrastructure.constants.JwtConstants.BEARER_PREFIX;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverUser;
import kr.spot.infrastructure.oauth.client.naver.NaverApiClient;
import kr.spot.infrastructure.oauth.client.naver.NaverAuthClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOauth {

  private final NaverAuthClient naverAuthClient;
  private final NaverApiClient naverApiClient;

  @Value("${spring.oauth2.naver.client-id}")
  private String NAVER_CLIENT_ID;
  @Value("${spring.oauth2.naver.client-secret}")
  private String NAVER_CLIENT_SECRET;
  @Value("${spring.oauth2.naver.callback-url}")
  private String NAVER_CALLBACK_LOGIN_URL;
  @Value("${spring.oauth2.naver.url}")
  private String NAVER_SNS_URL;

  public String getOauthRedirectURL() {
    Map<String, Object> params = new HashMap<>();
    params.put(CLIENT_ID, NAVER_CLIENT_ID);
    params.put(REDIRECT_URI, NAVER_CALLBACK_LOGIN_URL);
    params.put(RESPONSE_TYPE, RESPONSE_TYPE_CODE);
    params.put(STATE, STATE_STRING);

    String parameterString = params.entrySet().stream()
        .map(x -> x.getKey() + KEY_VALUE_DELIMITER + x.getValue())
        .collect(Collectors.joining(QUERY_DELIMITER));

    return NAVER_SNS_URL + QUERY_PREFIX + parameterString;
  }

  public NaverOAuthTokenDTO requestAccessToken(String code) {
    return naverAuthClient.getNaverAccessToken(
        GRANT_TYPE_AUTHORIZATION_CODE, NAVER_CLIENT_ID, NAVER_CLIENT_SECRET, code,
        NAVER_CALLBACK_LOGIN_URL);
  }

  public NaverUser requestUserInfo(String accessToken) {
    return naverApiClient.getNaverUserInfo(getAccessToken(accessToken));
  }

  private String getAccessToken(String accessToken) {
    return BEARER_PREFIX + accessToken;
  }
}
