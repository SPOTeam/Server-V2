package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.application.query.GetPostTestService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.query.dto.response.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글 조회 테스트")
@RestController
@RequestMapping("/api/test/posts")
@RequiredArgsConstructor
public class PostQueryTestController {

  private final GetPostTestService getPostService;

  @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @GetMapping("{postId}/members/{viewerId}/rdb")
  public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetailByRDB(
      @PathVariable Long postId,
      @PathVariable Long viewerId
  ) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._OK,
            getPostService.getPostDetailByRDB(postId, viewerId)));
  }

  @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @GetMapping("{postId}/members/{viewerId}/redis")
  public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetailByRedis(
      @PathVariable Long postId,
      @PathVariable Long viewerId
  ) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._OK,
            getPostService.getPostDetailByRedis(postId, viewerId)));
  }
}
