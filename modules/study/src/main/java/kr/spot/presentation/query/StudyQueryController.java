package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.query.GetMyStudyInfoService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
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

    private final GetMyStudyInfoService getMyStudyInfoService;

    @Operation(summary = "마이페이지 스터디 조회",
            description = "마이페이지에 필요한 스터디를 조회합니다.\n "
                    + "\n 모집 중인 스터디 : status = OWNER (기본값) \n"
                    + "\n 참여 중인 스터디 : status = APPROVED \n "
                    + "\n 대기 중인 스터디 : status = APPLIED")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<GetStudyOverviewResponse>> getMyStudies(
            @CurrentMember @Parameter(hidden = true) Long viewerId,
            @RequestParam(required = false, defaultValue = "OWNER") String status,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer size
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK,
                getMyStudyInfoService.getMyStudyOverview(viewerId, StudyMemberStatus.convert(status), cursor, size)));
    }

    @Operation(summary = "지금 가장 인기있는 스터디 조회",
            description = "지금 가장 인기있는 스터디를 조회합니다. (page, size 파라미터로 페이징 처리 가능)")
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<GetStudyOverviewResponse>> getHotStudies(
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
    }

    @Operation(summary = "추천하는 스터디 조회",
            description = "추천하는 스터디를 조회합니다. (page, size 파라미터로 페이징 처리 가능)")
    @GetMapping("/recommended")
    public ResponseEntity<ApiResponse<GetStudyOverviewResponse>> getRecommendedStudies(
    ) {
        return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, null));
    }

    // 필터링 O
    // 내 지역 스터디 - 아무 값 안넣으면 이 회원의 전체 관심 지역 스터디로 조회
    // 내 관심사 스터디 - 아무 값 안넣으면 이 회원의 전체 관심사 스터디로 조회
    // 모집중 스터디
    // 전체 스터디 조회
}
