package kr.spot.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.command.MemberWithdrawService;
import kr.spot.application.command.RegisterMemberInfoService;
import kr.spot.application.command.RegisterPreferredCategoryService;
import kr.spot.application.command.RegisterPreferredRegionService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.command.dto.request.RegisterPreferredCategoryRequest;
import kr.spot.presentation.command.dto.request.RegisterPreferredRegionRequest;
import kr.spot.presentation.command.dto.request.UpdateMemberNameRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원")
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberCommandController {

  private final RegisterPreferredCategoryService registerPreferredCategoryService;
  private final RegisterPreferredRegionService registerPreferredRegionService;
  private final RegisterMemberInfoService registerMemberInfoService;
  private final MemberWithdrawService memberWithdrawService;

  @Operation(summary = "선호 카테고리 등록", description = "회원의 선호 카테고리를 등록합니다. 한 번 저장 후 다시 호출하면 덮어씁니다. (이전 데이터 삭제)")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "등록 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `STUDY4001`: 존재하지 않는 카테고리입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/preferred-categories")
  public ResponseEntity<ApiResponse<Void>> registerPreferredCategories(
      @RequestBody RegisterPreferredCategoryRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    registerPreferredCategoryService.process(memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "선호 지역 등록", description = "회원의 선호 지역를 등록합니다. 한 번 저장 후 다시 호출하면 덮어씁니다. (이전 데이터 삭제)")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "등록 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `REGION4000`: 존재하지 않는 지역 코드입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/preferred-regions")
  public ResponseEntity<ApiResponse<Void>> registerPreferredRegions(
      @RequestBody RegisterPreferredRegionRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    registerPreferredRegionService.process(memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "회원 이름 수정", description = "회원의 이름을 수정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `MEMBER4005`: 이름 변경에 실패했습니다.
          - 이름은 공백일 수 없습니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/name")
  public ResponseEntity<ApiResponse<Void>> updateMemberName(
      @RequestBody UpdateMemberNameRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    registerMemberInfoService.updateMemberName(memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "회원 탈퇴", description = """
      ## 회원 탈퇴를 수행합니다.
      - 회원 정보 및 관련 데이터가 모두 삭제됩니다.
      - 클라이언트에서도 저장된 토큰을 삭제해주세요.
      """)
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "탈퇴 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @DeleteMapping("/me")
  public ResponseEntity<ApiResponse<Void>> withdraw(
      @CurrentMember @Parameter(hidden = true) long memberId
  ) {
    memberWithdrawService.withdraw(memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
  }
}
