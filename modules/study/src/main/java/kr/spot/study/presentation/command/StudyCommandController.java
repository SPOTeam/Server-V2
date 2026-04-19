package kr.spot.study.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.study.application.command.CreateStudyService;
import kr.spot.study.application.command.ReportStudyMemberService;
import kr.spot.study.application.command.StudyLikeService;
import kr.spot.study.application.command.UpdateStudyService;
import kr.spot.study.application.command.WithdrawStudyService;
import kr.spot.study.presentation.command.dto.request.CreateStudyRequest;
import kr.spot.study.presentation.command.dto.request.RepostStudyMemberRequest;
import kr.spot.study.presentation.command.dto.request.WithdrawStudyRequest;
import kr.spot.study.presentation.command.dto.response.CreateStudyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "스터디")
@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyCommandController {

  private final CreateStudyService createStudyService;
  private final UpdateStudyService updateStudyService;
  private final ReportStudyMemberService reportStudyMemberService;
  private final StudyLikeService studyLikeService;
  private final WithdrawStudyService withdrawStudyService;

  @Operation(summary = "스터디 생성", description =
      "새로운 스터디를 생성합니다. 요청 정보는 `multipart/form-data` 형식으로 보내야 하며, 이미지 파일은 선택 사항입니다."
          + "request는 application/json 형식으로 보내야 합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "생성 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `COMMON4000`: 잘못된 요청입니다. (예: 유효하지 않은 카테고리/스타일/지역 코드, 스터디 이름 공백, 최대 인원 수 음수, 유효하지 않은 스터디 비용)
          - `MEMBER4001`: 이름은 null 또는 공백일 수 없습니다.
          - `STUDY4000`: 최대 인원 수는 양수여야 합니다.
          - `STUDY4000`: 유효하지 않은 스터디 비용입니다.
          - `STUDY4001`: 존재하지 않는 카테고리입니다.
          - `REGION4000`: 존재하지 않는 지역 코드입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
          schemaProperties = {
              @SchemaProperty(name = "request", schema = @Schema(implementation = CreateStudyRequest.class)),
              @SchemaProperty(name = "imageFile", schema = @Schema(type = "string", format = "binary"))
          }))
  @PostMapping
  public ResponseEntity<ApiResponse<CreateStudyResponse>> createStudy(
      @RequestPart CreateStudyRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestPart(required = false) MultipartFile imageFile) {
    long studyId = createStudyService.createStudy(request, memberId, imageFile);
    return ResponseEntity.ok(
        ApiResponse.onSuccess(SuccessStatus._CREATED, CreateStudyResponse.from(studyId)));
  }

  @Operation(summary = "스터디 정보 수정", description =
      "스터디 정보를 수정합니다. 생성 API와 동일한 스펙으로 `multipart/form-data` 형식으로 보내야 하며, "
          + "이미지 파일은 선택 사항입니다. 이미지를 보내지 않으면 기존 이미지는 유지됩니다. "
          + "request는 application/json 형식으로 보내야 하며, 스터디장만 수정할 수 있습니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "수정 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = """
          - `COMMON4000`: 잘못된 요청입니다. (예: 유효하지 않은 카테고리/스타일/지역 코드, 스터디 이름 공백, 최대 인원 수 음수, 유효하지 않은 스터디 비용)
          - `MEMBER4001`: 이름은 null 또는 공백일 수 없습니다.
          - `STUDY4000`: 최대 인원 수는 양수여야 합니다.
          - `STUDY4000`: 유효하지 않은 스터디 비용입니다.
          - `STUDY4001`: 존재하지 않는 카테고리입니다.
          - `REGION4000`: 존재하지 않는 지역 코드입니다.
          """, content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "`STUDY4030`: 스터디장만 접근할 수 있습니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "`STUDY4040`: 존재하지 않는 스터디입니다.", content = @Content(schema = @Schema(implementation = kr.spot.ApiResponse.class)))
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
          schemaProperties = {
              @SchemaProperty(name = "request", schema = @Schema(implementation = CreateStudyRequest.class)),
              @SchemaProperty(name = "imageFile", schema = @Schema(type = "string", format = "binary"))
          }))
  @PatchMapping("/{studyId}")
  public ResponseEntity<ApiResponse<Void>> updateStudy(
      @PathVariable Long studyId,
      @RequestPart CreateStudyRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestPart(required = false) MultipartFile imageFile) {
    updateStudyService.updateStudy(studyId, request, memberId, imageFile);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 좋아요", description = "스터디에 좋아요를 누릅니다.")
  @PostMapping("/{studyId}/like")
  public ResponseEntity<ApiResponse<Void>> likeStudy(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    studyLikeService.likeStudy(studyId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 좋아요 취소", description = "스터디 좋아요를 취소합니다.")
  @DeleteMapping("/{studyId}/like")
  public ResponseEntity<ApiResponse<Void>> unlikeStudy(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    studyLikeService.unlikeStudy(studyId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 탈퇴", description = "스터디에서 탈퇴합니다.")
  @PostMapping("{studyId}/withdraw")
  public ResponseEntity<ApiResponse<Void>> withdrawStudy(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestBody WithdrawStudyRequest request
  ) {
    withdrawStudyService.withdrawStudy(studyId, memberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디원 신고", description = "스터디원(본인 제외)을 신고합니다.")
  @PostMapping("/{studyId}/members/{targetMemberId}/report")
  public ResponseEntity<ApiResponse<Void>> reportStudyMember(
      @PathVariable Long studyId,
      @PathVariable Long targetMemberId,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestBody RepostStudyMemberRequest request
  ) {
    reportStudyMemberService.reportStudyMember(studyId, targetMemberId, request);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "스터디 삭제", description = "스터디를 삭제합니다.")
  @DeleteMapping("/{studyId}")
  public ResponseEntity<ApiResponse<Void>> deleteStudy(
      @PathVariable Long studyId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    withdrawStudyService.deleteStudy(studyId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
