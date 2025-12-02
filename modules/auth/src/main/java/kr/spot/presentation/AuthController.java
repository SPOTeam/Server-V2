package kr.spot.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.application.token.TokenReissueService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

  private final TokenReissueService tokenReissueService;

  @Operation(summary = "토큰 재발급", description = """
      ## 리프레시 토큰을 통해 새로운 액세스 토큰과 리프레시 토큰을 발급받습니다.
      - **refreshToken**: 헤더에 담아 보내주세요. \n
      """)
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `COMMON4005`: JWT 토큰이 비어있습니다.
          - `COMMON4006`: 유효하지 않은 JWT token입니다.
          - `COMMON4007`: 만료된 JWT token입니다.
          - `COMMON4008`: 지원되지 않는 JWT token입니다.
          - `COMMON4010`: 유효하지 않은 리프레시 토큰입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/reissue")
  public ResponseEntity<ApiResponse<TokenDTO>> reissueToken(
      @RequestHeader String refreshToken
  ) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._OK, tokenReissueService.reissueToken(refreshToken)));
  }
}
