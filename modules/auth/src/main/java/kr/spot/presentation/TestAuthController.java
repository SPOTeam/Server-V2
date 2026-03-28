package kr.spot.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.application.test.CreateTestMemberService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.command.dto.TokenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestAuthController {

  private final CreateTestMemberService createTestMemberService;

  @Operation(summary = "테스트용 회원 생성", description = "테스트용 회원을 생성하고 1년 유효기간의 토큰을 반환합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK")
  })
  @PostMapping("/members")
  public ResponseEntity<ApiResponse<TokenDTO>> createTestMember() {
    TokenDTO tokenDTO = createTestMemberService.createTestMember();
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, tokenDTO));
  }
}
