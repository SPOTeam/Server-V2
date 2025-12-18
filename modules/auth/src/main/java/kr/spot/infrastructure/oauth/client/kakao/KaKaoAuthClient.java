package kr.spot.infrastructure.oauth.client.kakao;


import static kr.spot.infrastructure.constants.AuthConstants.CLIENT_ID;
import static kr.spot.infrastructure.constants.AuthConstants.GRANT_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.HEADER_CONTENT_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.REDIRECT_URI;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE_CODE;

import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoOAuthTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "kakaoAuthClient", url = "https://kauth.kakao.com")
public interface KaKaoAuthClient {

  @PostMapping(value = "/oauth/token")
  KaKaoOAuthTokenDTO getKaKaoAccessToken(
      @RequestHeader(HEADER_CONTENT_TYPE) String contentType,
      @RequestParam(GRANT_TYPE) String grant_type,
      @RequestParam(REDIRECT_URI) String redirectUri,
      @RequestParam(CLIENT_ID) String client_id,
      @RequestParam(RESPONSE_TYPE_CODE) String code);
}
