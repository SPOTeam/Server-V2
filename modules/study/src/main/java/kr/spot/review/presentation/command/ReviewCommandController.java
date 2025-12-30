package kr.spot.review.presentation.command;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.review.application.command.ManageReviewReactionService;
import kr.spot.review.application.command.ManageReviewService;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.presentation.command.dto.CreateReviewRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "스터디 회고록")
@RestController
@RequestMapping("/api/studies/{studyId}/reviews")
@RequiredArgsConstructor
public class ReviewCommandController {

  private final ManageReviewService manageReviewService;
  private final ManageReviewReactionService manageReviewReactionService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createReview(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestPart CreateReviewRequest request,
      @RequestPart(required = false) MultipartFile imageFile
  ) {
    manageReviewService.createReview(studyId, memberId, request, imageFile);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @DeleteMapping("/{reviewId}")
  public ResponseEntity<ApiResponse<Void>> deleteReview(
      @PathVariable Long studyId,
      @PathVariable Long reviewId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    manageReviewService.deleteReview(studyId, reviewId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }

  @PostMapping("/{reviewId}/reactions")
  public ResponseEntity<ApiResponse<Void>> addReaction(
      @PathVariable Long studyId,
      @PathVariable Long reviewId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestParam Reaction reaction
  ) {
    manageReviewReactionService.addReaction(studyId, reviewId, memberId, reaction);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @DeleteMapping("/{reviewId}/reactions")
  public ResponseEntity<ApiResponse<Void>> removeReaction(
      @PathVariable Long studyId,
      @PathVariable Long reviewId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestParam Reaction reaction
  ) {
    manageReviewReactionService.removeReaction(studyId, reviewId, memberId, reaction);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._NO_CONTENT));
  }
}
