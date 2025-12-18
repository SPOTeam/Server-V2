package kr.spot.infrastructure.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoOAuthTokenDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser.KaKaoAccountDTO;
import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser.KaKaoPropertiesDTO;
import kr.spot.infrastructure.oauth.client.kakao.KaKaoApiClient;
import kr.spot.infrastructure.oauth.client.kakao.KaKaoAuthClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class KaKaoOauthTest {

  @Mock
  KaKaoApiClient kaKaoApiClient;

  @Mock
  KaKaoAuthClient kaKaoAuthClient;

  KaKaoOauth kakao;

  @BeforeEach
  void setUp() {
    kakao = new KaKaoOauth(kaKaoApiClient, kaKaoAuthClient);
    // Inject @Value fields manually for testing
    ReflectionTestUtils.setField(kakao, "KAKAO_SNS_URL", "https://kauth.kakao.com/oauth/authorize");
    ReflectionTestUtils.setField(kakao, "KAKAO_SNS_CLIENT_ID", "client-123");
    ReflectionTestUtils.setField(kakao, "KAKAO_SNS_CALLBACK_LOGIN_URL", "https://my.app/callback");
  }

  @Test
  @DisplayName("리다이렉트 URL이 올바르게 생성된다")
  void should_build_redirect_url_correctly() {
    String url = kakao.getOauthRedirectURL();

    assertThat(url)
        .startsWith("https://kauth.kakao.com/oauth/authorize?")
        .contains("client_id=client-123")
        .contains("redirect_uri=https://my.app/callback")
        .contains("response_type=code");
  }

  @Test
  @DisplayName("인가 코드를 이용해 액세스 토큰을 요청한다")
  void should_request_access_token_with_correct_parameters() {
    // given
    String code = "abc";
    KaKaoOAuthTokenDTO token = new KaKaoOAuthTokenDTO("atk", "atk", null, null, null);
    when(kaKaoAuthClient.getKaKaoAccessToken(
        "application/x-www-form-urlencoded;charset=utf-8",
        "authorization_code",
        "https://my.app/callback",
        "client-123",
        code)
    ).thenReturn(token);

    // when
    KaKaoOAuthTokenDTO result = kakao.requestAccessToken(code);

    // then
    assertThat(result.access_token()).isEqualTo("atk");
    verify(kaKaoAuthClient).getKaKaoAccessToken(
        "application/x-www-form-urlencoded;charset=utf-8",
        "authorization_code",
        "https://my.app/callback",
        "client-123",
        "abc"
    );
  }

  @Test
  @DisplayName("액세스 토큰으로 사용자 정보를 요청한다")
  void should_request_user_info_with_bearer_token() {
    // given
    KaKaoOAuthTokenDTO token = new KaKaoOAuthTokenDTO("atk", "atk", null, null, null);
    KaKaoUser mockUser = createFakeKaKaoUser();
    when(kaKaoApiClient.getKaKaoUserInfo("Bearer atk",
        "application/x-www-form-urlencoded;charset=utf-8"))
        .thenReturn(mockUser);

    // when
    KaKaoUser user = kakao.requestUserInfo(token.access_token());

    // then
    assertThat(user).isNotNull();
    verify(kaKaoApiClient).getKaKaoUserInfo(
        "Bearer atk",
        "application/x-www-form-urlencoded;charset=utf-8"
    );
  }

  private KaKaoUser createFakeKaKaoUser() {
    return new KaKaoUser(1L, "now",
        new KaKaoPropertiesDTO(
            "nickname", "image", "thumbnaile"),
        new KaKaoAccountDTO(
            true, true, true, true, "email"
        ));
  }
}
