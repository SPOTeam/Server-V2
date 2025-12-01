package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.query.GetPostService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.domain.enums.HotPostSortBy;
import kr.spot.domain.enums.PostType;
import kr.spot.presentation.query.dto.response.PostDetailResponse;
import kr.spot.presentation.query.dto.response.PostListResponse;
import kr.spot.presentation.query.dto.response.PostOverviewResponse;
import kr.spot.presentation.query.dto.response.RecentPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "게시글")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostQueryController {

    private final GetPostService getPostService;

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`POST404`: 게시글을 찾을 수 없습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
    })
    @GetMapping("{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> getPostDetail(
            @PathVariable Long postId,
            @CurrentMember @Parameter(hidden = true) Long viewerId
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessStatus._OK, getPostService.getPostDetail(postId, viewerId)));
    }

    @Operation(summary = "게시글 리스트 조회", description = "게시글 리스트를 조회합니다. 마지막으로 본 게시글 이후의 게시글들을 페이징하여 가져옵니다."
            + "게시글 유형별로 필터링이 가능합니다. 아무 조건을 입력하지 않은 경우, 전체 유형을 대상으로 조회합니다. "
            + "또한 글자 수가 많은 게시글의 경우 일부 내용이 생략되어 제공됩니다. (현재 기준은 100자)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "size 파라미터는 1과 50 사이여야 합니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponse<PostListResponse>> getPostList(
            @CurrentMember @Parameter(hidden = true) Long viewerId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(required = false) PostType postType,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer size
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessStatus._OK, getPostService.getPostList(postType, cursor, viewerId, size)));
    }


    @Operation(summary = "BEST 인기글 조회", description = "인기 게시글 3개를 조회합니다.")
    @GetMapping("/hot")
    public ResponseEntity<ApiResponse<PostOverviewResponse>> getHotPosts(
            @RequestParam HotPostSortBy sortBy
    ) {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessStatus._OK, getPostService.getHotPosts(sortBy)));
    }

    @Operation(summary = "최근 게시글 조회", description = "게시글 종류 별 최신 글을 조회합니다.")
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<RecentPostResponse>> getRecentPosts() {
        return ResponseEntity.ok(
                ApiResponse.onSuccess(SuccessStatus._OK, getPostService.getRecentPosts()));
    }


}
