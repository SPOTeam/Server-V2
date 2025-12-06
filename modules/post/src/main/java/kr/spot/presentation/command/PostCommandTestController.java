package kr.spot.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.application.command.LikePostTestService;
import kr.spot.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글 테스트")
@RestController
@RequestMapping("/api/test/posts/")
@RequiredArgsConstructor
public class PostCommandTestController {

  private final LikePostTestService likePostService;

  @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "`POST4000`: 이미 좋아요를 누른 게시글입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/{postId}/members/{writerId}/like")
  public ResponseEntity<ApiResponse<Void>> likePost(
      @PathVariable Long postId,
      @PathVariable Long writerId) {
    likePostService.likePost(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 좋아요", description = "테스트 용 : 낙관 락 \n"
      + "특정 게시글에 좋아요를 추가합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "`POST4000`: 이미 좋아요를 누른 게시글입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/{postId}/members/{writerId}/like/by-optimistic-lock")
  public ResponseEntity<ApiResponse<Void>> likePostByOptimisticLock(
      @PathVariable Long postId,
      @PathVariable Long writerId) {
    likePostService.likePostOptimistic(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 좋아요", description = "테스트 용 : 비관 락 \n"
      + "특정 게시글에 좋아요를 추가합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "`POST4000`: 이미 좋아요를 누른 게시글입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/{postId}/members/{writerId}/like-by-pessimistic-lock")
  public ResponseEntity<ApiResponse<Void>> likePostPessimisticLock(
      @PathVariable Long postId,
      @PathVariable Long writerId) {
    likePostService.likePostPessimistic(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

}
