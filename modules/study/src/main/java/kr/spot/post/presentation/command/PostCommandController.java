package kr.spot.post.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.post.application.command.LikePostService;
import kr.spot.post.application.command.ManagePostService;
import kr.spot.post.presentation.command.dto.CreatePostResponse;
import kr.spot.post.presentation.command.dto.ManagePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 게시글")
@RestController("studyPostCommandController")
@RequestMapping("/api/studies/{studyId}/posts")
@RequiredArgsConstructor
public class PostCommandController {

  private final ManagePostService managePostService;
  private final LikePostService likePostService;

  @Operation(summary = "스터디 게시글 생성", description = "스터디에 새로운 게시글을 작성합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`STUDY404`: 스터디를 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping
  public ResponseEntity<ApiResponse<CreatePostResponse>> createPost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long writerId,
      @RequestBody ManagePostRequest request) {
    long postId = managePostService.createPost(studyId, writerId, request);
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._CREATED, CreatePostResponse.from(postId)));
  }

  @Operation(summary = "스터디 게시글 수정", description = "스터디의 게시글을 수정합니다. 작성자만 수정할 수 있습니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`POST403`: 작성자만 수정할 수 있습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PutMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> updatePost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long writerId,
      @RequestBody ManagePostRequest request) {
    managePostService.updatePost(studyId, postId, request, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 게시글 삭제", description = "스터디의 게시글을 삭제합니다. 작성자만 삭제할 수 있습니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`POST403`: 작성자만 삭제할 수 있습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long writerId) {
    managePostService.deletePost(studyId, postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 게시글 핀", description = "스터디의 게시글을 상단에 고정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 핀 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`STUDY403`: 해당 스터디에 대한 접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/{postId}/pin")
  public ResponseEntity<ApiResponse<Void>> pinPost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    managePostService.pinPost(studyId, postId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 게시글 핀 해제", description = "스터디의 게시글 상단 고정을 해제합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "게시글 핀 해제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`STUDY403`: 해당 스터디에 대한 접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/{postId}/pin")
  public ResponseEntity<ApiResponse<Void>> unpinPost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    managePostService.unpinPost(studyId, postId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 게시글 좋아요", description = "스터디 게시글에 좋아요를 추가합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`STUDY403`: 해당 스터디에 대한 접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/{postId}/like")
  public ResponseEntity<ApiResponse<Void>> likePost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    likePostService.likePost(studyId, postId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 게시글 좋아요 취소", description = "스터디 게시글의 좋아요를 취소합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "좋아요 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`STUDY403`: 해당 스터디에 대한 접근 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/{postId}/like")
  public ResponseEntity<ApiResponse<Void>> unlikePost(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "게시글 ID", required = true) @PathVariable Long postId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    likePostService.unlikePost(studyId, postId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
