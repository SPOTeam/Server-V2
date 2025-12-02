package kr.spot.infrastructure.oauth.client.naver;


import static kr.spot.infrastructure.constants.AuthConstants.HEADER_AUTHORIZATION;

import kr.spot.infrastructure.oauth.client.dto.oauth.naver.NaverUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "naverApiClient", url = "https://openapi.naver.com")
public interface NaverApiClient {

  @GetMapping("/v1/nid/me")
  NaverUser getNaverUserInfo(
      @RequestHeader(HEADER_AUTHORIZATION) String accessToken);
}
