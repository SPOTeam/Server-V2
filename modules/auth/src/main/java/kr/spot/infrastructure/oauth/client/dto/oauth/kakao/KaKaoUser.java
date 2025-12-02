package kr.spot.infrastructure.oauth.client.dto.oauth.kakao;

public record KaKaoUser(
    Long id,
    String connected_at,
    KaKaoPropertiesDTO properties,
    KaKaoAccountDTO kakao_account
) {

  public record KaKaoAccountDTO(
      Boolean has_email,
      Boolean email_needs_agreement,
      Boolean is_email_valid,
      Boolean is_email_verified,
      String email
  ) {

  }

  public record KaKaoPropertiesDTO(
      String nickname,
      String profile_image,
      String thumbnail_image
  ) {

  }
}
