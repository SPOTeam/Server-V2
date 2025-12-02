package kr.spot.infrastructure.oauth.client.kakao;

import static kr.spot.infrastructure.constants.AuthConstants.HEADER_AUTHORIZATION;
import static kr.spot.infrastructure.constants.AuthConstants.HEADER_CONTENT_TYPE;

import kr.spot.infrastructure.oauth.client.dto.oauth.kakao.KaKaoUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "kakaoApiClient", url = "https://kapi.kakao.com")
public interface KaKaoApiClient {

  @GetMapping("/v2/user/me")
  KaKaoUser getKaKaoUserInfo(
      @RequestHeader(HEADER_AUTHORIZATION) String accessToken,
      @RequestHeader(HEADER_CONTENT_TYPE) String contentType);
}
