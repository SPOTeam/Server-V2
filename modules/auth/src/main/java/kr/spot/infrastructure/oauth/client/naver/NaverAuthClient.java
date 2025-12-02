package kr.spot.infrastructure.oauth.client.naver;

import static kr.spot.infrastructure.constants.AuthConstants.CLIENT_ID;
import static kr.spot.infrastructure.constants.AuthConstants.CLIENT_SECRET;
import static kr.spot.infrastructure.constants.AuthConstants.GRANT_TYPE;
import static kr.spot.infrastructure.constants.AuthConstants.RESPONSE_TYPE_CODE;
import static kr.spot.infrastructure.constants.AuthConstants.STATE;

import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverOAuthTokenDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "naverAuthClient", url = "https://nid.naver.com/oauth2.0")
public interface NaverAuthClient {

  @GetMapping("/token")
  NaverOAuthTokenDTO getNaverAccessToken(
      @RequestParam(GRANT_TYPE) String grantType,
      @RequestParam(CLIENT_ID) String clientId,
      @RequestParam(CLIENT_SECRET) String clientSecret,
      @RequestParam(RESPONSE_TYPE_CODE) String code,
      @RequestParam(STATE) String state
  );
}
