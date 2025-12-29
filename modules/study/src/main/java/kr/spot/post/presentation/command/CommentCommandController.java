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
import kr.spot.post.application.command.ManageCommentService;
import kr.spot.post.presentation.command.dto.CreateCommentResponse;
import kr.spot.post.presentation.command.dto.ManageCommentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 게시글 - 댓글")
@RestController("studyCommentCommandController")
@RequestMapping("/api/studies/{studyId}/posts")
@RequiredArgsConstructor
public class CommentCommandController {

  private final ManageCommentService commentService;

  @Operation(summary = "댓글 작성", description = "특정 게시글에 새로운 댓글을 작성합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "작성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = """
          - `MEMBER404`: 회원을 찾을 수 없습니다.
          - `POST404`: 게시글을 찾을 수 없습니다.
          """, content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PostMapping("/{postId}/comments")
  public ResponseEntity<ApiResponse<CreateCommentResponse>> createComment(
      @CurrentMember @Parameter(hidden = true) Long writerId,
      @PathVariable Long studyId,
      @PathVariable Long postId,
      @RequestBody ManageCommentRequest request) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED,
        commentService.createComment(writerId, postId, studyId, request)));
  }

  @Operation(summary = "댓글 수정", description = "기존 댓글을 수정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "수정 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`COMMENT404`: 댓글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @PutMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> updateComment(
      @PathVariable Long commentId,
      @PathVariable Long studyId,
      @RequestBody ManageCommentRequest request,
      @CurrentMember @Parameter(hidden = true) Long writerId) {
    commentService.updateComment(writerId, commentId, studyId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @Operation(summary = "댓글 삭제", description = "기존 댓글을 삭제합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "삭제 권한이 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`COMMENT404`: 댓글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @PathVariable Long commentId,
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long writerId
  ) {
    commentService.deleteComment(writerId, commentId, studyId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

}
