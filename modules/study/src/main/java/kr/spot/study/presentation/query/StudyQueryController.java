package kr.spot.study.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.study.application.query.GetStudyDetailService;
import kr.spot.study.presentation.query.dto.response.GetStudyInfoResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyMembersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 조회")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyQueryController {

  private final GetStudyDetailService getStudyDetailService;

  @Operation(summary = "스터디 상세 정보 조회",
      description = "스터디의 상세 정보를 조회합니다.")
  @GetMapping("/info")
  public ResponseEntity<ApiResponse<GetStudyInfoResponse>> getStudyInfo(
      @RequestParam Long studyId,
      @CurrentMember @Parameter(hidden = true) Long viewerId
  ) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
        getStudyDetailService.getStudyInfo(studyId, viewerId)));
  }

  @Operation(summary = "스터디 멤버 조회",
      description = "스터디의 멤버들을 조회합니다.")
  @GetMapping("/members")
  public ResponseEntity<ApiResponse<GetStudyMembersResponse>> getStudyMembers(
      @RequestParam Long studyId
  ) {
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
        getStudyDetailService.getStudyMembers(studyId)));
  }


}
