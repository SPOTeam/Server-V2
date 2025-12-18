package kr.spot.infrastructure.oauth.client.dto.oauth.kakao;

public record KaKaoOAuthTokenDTO(
    String token_type,
    String access_token,
    String refresh_token,
    String expires_in,
    String refresh_token_expires_in
) {

}
