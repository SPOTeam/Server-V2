package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.query.dto.response.GetAppliesResponse;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 신청", description = "스터디 신청 관련 API")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyApplyQueryController {

    @Operation(summary = "승인된 스터디 내역 조회", description = "내가 신청해서 승인된 스터디 내역을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GetMyAppliedStudyResponse.class)))
    })
    @GetMapping("/applied")
    public ResponseEntity<ApiResponse<GetMyAppliedStudyResponse>> getMyAppliedStudies(
            @Parameter(hidden = true) @CurrentMember Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
    }

    @Operation(summary = "스터디 신청 내역 조회", description = "특정 스터디의 신청 내역을 조회합니다. (스터디장 권한)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = GetAppliesResponse.class)))
    })
    @GetMapping("/{studyId}/applications")
    public ResponseEntity<ApiResponse<GetAppliesResponse>> getStudyApplies(
            @Parameter(name = "studyId", description = "스터디 ID", in = ParameterIn.PATH) @PathVariable Long studyId,
            @Parameter(hidden = true) @CurrentMember Long memberId
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
    }
}
