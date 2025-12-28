package kr.spot.post.presentation.command;

import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.post.application.command.ManagePostService;
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

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createPost(
      @PathVariable Long studyId,
      @CurrentMember Long writerId,
      @RequestBody ManagePostRequest request) {
    managePostService.createPost(studyId, writerId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @PutMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> updatePost(
      @PathVariable Long studyId,
      @PathVariable Long postId,
      @CurrentMember Long writerId,
      @RequestBody ManagePostRequest request) {
    managePostService.updatePost(studyId, postId, request, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @PathVariable Long studyId,
      @PathVariable Long postId,
      @CurrentMember Long writerId) {
    managePostService.deletePost(studyId, postId, writerId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @PostMapping("/{postId}/pin")
  public ResponseEntity<ApiResponse<Void>> pinPost(
      @PathVariable Long studyId,
      @PathVariable Long postId) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }
}
