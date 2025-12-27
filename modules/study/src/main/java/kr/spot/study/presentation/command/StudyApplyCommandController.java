package kr.spot.study.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.study.application.command.ApplyStudyService;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.presentation.command.dto.request.ApplyStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 신청", description = "스터디 신청 관련 API")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyApplyCommandController {

  private final ApplyStudyService applyStudyService;

  @Operation(summary = "스터디 신청", description = "특정 스터디에 참여 신청을 합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스터디 신청 성공")
  })
  @PostMapping("/{studyId}/apply")
  public ResponseEntity<ApiResponse<Void>> applyStudy(
      @Parameter(name = "studyId", description = "스터디 ID", in = ParameterIn.PATH) @PathVariable Long studyId,
      @Parameter(hidden = true) @CurrentMember Long memberId,
      @RequestBody ApplyStudyRequest request) {
    applyStudyService.applyStudy(studyId, memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
  }

  @Operation(summary = "스터디 신청 승인/거절", description = "스터디 신청을 승인하거나 거절합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스터디 신청 처리 성공")
  })
  @PostMapping("/applications/{applicationId}")
  public ResponseEntity<ApiResponse<Void>> approveStudyApplication(
      @Parameter(name = "applicationId", description = "신청 ID", in = ParameterIn.PATH) @PathVariable Long applicationId,
      @Parameter(hidden = true) @CurrentMember Long memberId,
      @Parameter(name = "decision", description = "승인(APPROVE) 또는 거절(REJECT)") @RequestParam Decision decision) {
    applyStudyService.processStudyApplication(applicationId, memberId, decision);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
  }

  @Operation(summary = "스터디 최종 참가 여부 결정", description = "승인된 스터디에 최종 참가할지 여부를 결정합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "스터디 참가 여부 결정 성공")
  })
  @PostMapping("/{studyId}/decide")
  public ResponseEntity<ApiResponse<Void>> decideStudyParticipation(
      @Parameter(name = "studyId", description = "스터디 ID", in = ParameterIn.PATH) @PathVariable Long studyId,
      @Parameter(hidden = true) @CurrentMember Long memberId,
      @Parameter(name = "decision", description = "승인(APPROVE) 또는 거절(REJECT)") @RequestParam Decision decision
  ) {
    applyStudyService.decideFinalParticipation(studyId, memberId, decision);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
  }
}
