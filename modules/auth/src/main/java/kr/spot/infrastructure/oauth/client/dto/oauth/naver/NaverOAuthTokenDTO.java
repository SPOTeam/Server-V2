package kr.spot.infrastructure.oauth.client.dto.oauth.naver;

public record NaverOAuthTokenDTO(
    String access_token,
    String token_type,
    String refresh_token,
    String expires_in,
    String error,
    String error_description
) {

}
