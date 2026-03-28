package kr.spot.presentation.command;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.command.LikePostService;
import kr.spot.application.command.ManagePostService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.command.dto.request.ManagePostRequest;
import kr.spot.presentation.command.dto.request.ReportPostRequest;
import kr.spot.presentation.command.dto.response.CreatePostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "게시글")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostCommandController {

  private final ManagePostService managePostService;
  private final LikePostService likePostService;

  @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`MEMBER404`: 회원을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
      @RequestPart ManagePostRequest request,
      @CurrentMember @Parameter(hidden = true) Long writerId,
      @RequestPart(required = false) MultipartFile imageFile
  ) {
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._CREATED,
            managePostService.createPost(request, writerId, imageFile)));
  }

  @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`POST403`: 수정 권한이 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PutMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> updatePost(
      @PathVariable Long postId,
      @RequestPart ManagePostRequest request,
      @CurrentMember @Parameter(hidden = true) Long writerId,
      @RequestPart(required = false) MultipartFile imageFile
  ) {
    managePostService.updatePost(postId, request, writerId, imageFile);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 삭제", description = "기존 게시글을 삭제합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`POST403`: 삭제 권한이 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long writerId) {
    managePostService.deletePost(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 좋아요", description = "특정 게시글에 좋아요를 추가합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "`POST4000`: 이미 좋아요를 누른 게시글입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/{postId}/like")
  public ResponseEntity<ApiResponse<Void>> likePost(
      @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long writerId) {
    likePostService.likePost(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 좋아요 취소", description = "특정 게시글에 좋아요를 취소합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "좋아요 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "`POST4001`: 좋아요를 누르지 않은 게시글입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @DeleteMapping("/{postId}/unlike")
  public ResponseEntity<ApiResponse<Void>> unlikePost(
      @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long writerId) {
    likePostService.unlikePost(postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "게시글 신고", description = "특정 게시글을 신고합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "신고 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @PostMapping("/{postId}/report")
  public ResponseEntity<ApiResponse<Void>> reportPost(
      @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long reporterId,
      @RequestBody ReportPostRequest request) {
    managePostService.reportPost(postId, reporterId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }
}
