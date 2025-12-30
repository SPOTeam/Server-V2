package kr.spot.review.presentation.query;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.review.application.query.GetReviewService;
import kr.spot.review.presentation.query.dto.GetReviewListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 회고록")
@RestController
@RequestMapping("/api/studies/{studyId}/reviews")
@RequiredArgsConstructor
public class ReviewQueryController {

  private final GetReviewService getReviewService;

  @GetMapping
  public ResponseEntity<ApiResponse<GetReviewListResponse>> getReviewList(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestParam(required = false) Long cursor,
      @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer size
  ) {
    GetReviewListResponse response = getReviewService.getReviewList(studyId, memberId, cursor, size);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
