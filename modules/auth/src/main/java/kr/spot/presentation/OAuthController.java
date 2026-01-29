package kr.spot.presentation;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.application.oauth.OAuthService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.domain.enums.LoginType;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "소셜 로그인")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
public class OAuthController {

  private final OAuthService oAuthService;

  @Operation(summary = "소셜 로그인 리다이렉트 URL 반환",
      description = "소셜 로그인 타입에 따라 리다이렉트 URL을 반환합니다. " +
          "예: /api/oauth/redirect-url/naver")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `MEMBER4003`: 지원하지 않는 로그인 타입입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @GetMapping("/redirect-url/{type}")
  public ResponseEntity<ApiResponse<String>> getRedirectUrl(
      @PathVariable @Parameter(description =
          "소셜 로그인 타입(naver, kakao)", required = true) String type) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
        oAuthService.redirectURL(LoginType.valueOf(type.toUpperCase()))));
  }

  @Operation(summary = "소셜 로그인 콜백 처리",
      description = "소셜 로그인 후 받은 code를 통해 로그인 또는 회원가입을 처리합니다. " +
          "예: /api/oauth/callback/naver?code=YOUR_CODE")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `MEMBER4003`: 지원하지 않는 로그인 타입입니다.
          - `MEMBER4000`: 유효하지 않은 이메일 형식입니다.
          - 소셜 로그인 `code`가 유효하지 않은 경우
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @GetMapping("/callback/{type}")
  public ResponseEntity<ApiResponse<TokenDTO>> socialLoginCallback(
      @PathVariable @Parameter(description = "소셜 로그인 타입(naver, kakao)", required = true) String type,
      @Parameter(description = "소셜 로그인 후 발급받은 code", required = true)
      @RequestParam("code") String code) {

    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
        oAuthService.getOAuthProfile(LoginType.valueOf(type.toUpperCase()), code)));
  }

  @Operation(summary = "소셜 로그인 클라이언트용 처리",
      description = "클라이언트에서 받은 액세스 토큰을 통해 로그인 또는 회원가입을 처리합니다. " +
          "예: /api/oauth/client/kakao?accessToken=YOUR_ACCESS_TOKEN")
  @GetMapping("/client/{type}")
  public ResponseEntity<ApiResponse<TokenDTO>> socialLoginForClient(
      @PathVariable String type,
      @RequestParam("accessToken") String accessToken
  ) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
        oAuthService.getOAuthProfileForClient(LoginType.valueOf(type.toUpperCase()), accessToken)));
  }
}