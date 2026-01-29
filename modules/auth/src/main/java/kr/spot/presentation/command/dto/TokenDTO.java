package kr.spot.presentation.command.dto;

public record TokenDTO(Long id, String accessToken, String refreshToken) {

  public static TokenDTO of(Long id, String accessToken, String refreshToken) {
    return new TokenDTO(id, accessToken, refreshToken);
  }
}
