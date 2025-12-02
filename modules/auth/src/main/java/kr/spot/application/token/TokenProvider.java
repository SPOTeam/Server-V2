package kr.spot.application.token;


import kr.spot.presentation.command.dto.TokenDTO;

public interface TokenProvider {

  TokenDTO createToken(Long memberId);

  void validateToken(String token);

  Long getMemberIdByToken(String token);
}
