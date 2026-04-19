package kr.spot.presentation.command.dto;

public record TokenDTO(Long id, String accessToken, String refreshToken, boolean isNewMember) {

  public static TokenDTO of(Long id, String accessToken, String refreshToken) {
    return new TokenDTO(id, accessToken, refreshToken, false);
  }

  public static TokenDTO of(Long id, String accessToken, String refreshToken, boolean isNewMember) {
    return new TokenDTO(id, accessToken, refreshToken, isNewMember);
  }
}
