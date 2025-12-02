package kr.spot.infrastructure.oauth;

import static kr.spot.infrastructure.constants.AuthConstants.CLIENT_ID;
import static kr.spot.infrastructure.constants.AuthConstants.CONTENT_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.GRANT_TYPE_AUTHORIZATION_CODE;
import static kr.spot.infrastructure.constants.AuthConstants.KEY_VALUE_DELIMITER;
import static kr.spot.infrastructure.constants.AuthConstants.QUERY_DELIMITER;
import static kr.spot.infrastructure.constants.AuthConstants.QUERY_PREFIX;
import static kr.spot.infrastructure.constants.AuthConstants.REDIRECT_URI;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE_CODE;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import kr.spot.infrastructure.constants.JwtConstants;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser;
import kr.spot.infrastructure.oauth.client.kakao.KaKaoApiClient;
import kr.spot.infrastructure.oauth.client.kakao.KaKaoAuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KaKaoOauth {

  private final KaKaoApiClient kaKaoApiClient;
  private final KaKaoAuthClient kaKaoAuthClient;

  @Value("${spring.oauth2.kakao.url}")
  private String KAKAO_SNS_URL;
  @Value("${spring.oauth2.kakao.client-id}")
  private String KAKAO_SNS_CLIENT_ID;
  @Value("${spring.oauth2.kakao.callback-login-url}")
  private String KAKAO_SNS_CALLBACK_LOGIN_URL;

  private static String getAccessToken(String accessToken) {
    return JwtConstants.BEARER_PREFIX + accessToken;
  }

  public String getOauthRedirectURL() {
    Map<String, Object> params = new HashMap<>();
    params.put(CLIENT_ID, KAKAO_SNS_CLIENT_ID);
    params.put(REDIRECT_URI, KAKAO_SNS_CALLBACK_LOGIN_URL);
    params.put(RESPONSE_TYPE, RESPONSE_TYPE_CODE);

    String parameterString = params.entrySet().stream()
        .map(x -> x.getKey() + KEY_VALUE_DELIMITER + x.getValue())
        .collect(Collectors.joining(QUERY_DELIMITER));

    return KAKAO_SNS_URL + QUERY_PREFIX + parameterString;
  }

  public KaKaoOAuthTokenDTO requestAccessToken(String code) {
    return kaKaoAuthClient.getKaKaoAccessToken(
        CONTENT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE, KAKAO_SNS_CALLBACK_LOGIN_URL,
        KAKAO_SNS_CLIENT_ID, code);
  }

  public KaKaoUser requestUserInfo(String accessToken) {
    return kaKaoApiClient.getKaKaoUserInfo(getAccessToken(accessToken), CONTENT_TYPE);
  }


}
