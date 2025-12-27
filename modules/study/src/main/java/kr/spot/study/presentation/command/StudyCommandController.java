package kr.spot.study.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.study.application.command.CreateStudyService;
import kr.spot.study.presentation.command.dto.request.CreateStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/studies")
@RequiredArgsConstructor
public class StudyCommandController {

  private final CreateStudyService createStudyService;

  @Tag(name = "스터디")
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
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createStudy(@RequestPart CreateStudyRequest request,
      @CurrentMember @Parameter(hidden = true) Long memberId,
      @RequestPart(required = false) MultipartFile imageFile) {
    createStudyService.createStudy(request, memberId, imageFile);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }
}
