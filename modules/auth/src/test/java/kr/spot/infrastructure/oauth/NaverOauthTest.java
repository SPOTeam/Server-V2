package kr.spot.infrastructure.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverUser;
import kr.spot.infrastructure.oauth.client.naver.NaverApiClient;
import kr.spot.infrastructure.oauth.client.naver.NaverAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class NaverOauthTest {

  @Mock
  NaverAuthClient naverAuthClient;

  @Mock
  NaverApiClient naverApiClient;

  NaverOauth naver;

  @BeforeEach
  void setUp() {
    naver = new NaverOauth(naverAuthClient, naverApiClient);
    ReflectionTestUtils.setField(naver, "NAVER_CLIENT_ID", "client-id");
    ReflectionTestUtils.setField(naver, "NAVER_CLIENT_SECRET", "client-secret");
    ReflectionTestUtils.setField(naver, "NAVER_CALLBACK_LOGIN_URL", "https://my.app/callback");
    ReflectionTestUtils.setField(naver, "NAVER_SNS_URL",
        "https://nid.naver.com/oauth2.0/authorize");
  }

  @Test
  @DisplayName("리다이렉트 URL이 올바르게 생성된다")
  void should_build_redirect_url_correctly() {
    String url = naver.getOauthRedirectURL();

    assertThat(url)
        .startsWith("https://nid.naver.com/oauth2.0/authorize?")
        .contains("client_id=client-id")
        .contains("redirect_uri=https://my.app/callback")
        .contains("response_type=code")
        .contains("state=STATE_STRING");
  }

  @Test
  @DisplayName("인가 코드를 통해 액세스 토큰을 요청한다")
  void should_request_access_token_with_code() {
    // given
    String code = "abc123";
    NaverOAuthTokenDTO token = new NaverOAuthTokenDTO("atk", "rtk", null, null, null, null);

    when(naverAuthClient.getNaverAccessToken(
        "authorization_code",
        "client-id",
        "client-secret",
        code,
        "https://my.app/callback"
    )).thenReturn(token);

    // when
    NaverOAuthTokenDTO result = naver.requestAccessToken(code);

    // then
    assertThat(result.access_token()).isEqualTo("atk");
    verify(naverAuthClient).getNaverAccessToken(
        "authorization_code",
        "client-id",
        "client-secret",
        code,
        "https://my.app/callback"
    );
  }

  @Test
  @DisplayName("액세스 토큰으로 사용자 정보를 요청한다")
  void should_request_user_info_with_token() {
    // given
    NaverOAuthTokenDTO token = new NaverOAuthTokenDTO("atk", "atk", null, null, null, null);
    NaverUser user = new NaverUser(null, null, null);

    when(naverApiClient.getNaverUserInfo("Bearer atk")).thenReturn(user);

    // when
    NaverUser result = naver.requestUserInfo(token.access_token());

    // then
    assertThat(result).isNotNull();
    verify(naverApiClient).getNaverUserInfo("Bearer atk");
  }
}
