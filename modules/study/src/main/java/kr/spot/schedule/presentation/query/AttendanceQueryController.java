package kr.spot.schedule.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.code.status.SuccessStatus;
import kr.spot.schedule.application.query.GetAttendanceService;
import kr.spot.schedule.presentation.query.dto.GetAttendanceInfoResponse;
import kr.spot.schedule.presentation.query.dto.GetAttendanceListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정 - 출석체크", description = "스터디 일정 출석체크 관련 API")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules/{scheduleId}/attendance")
@RequiredArgsConstructor
public class AttendanceQueryController {

  private final GetAttendanceService getAttendanceService;

  @Operation(
      summary = "출석 목록 조회",
      description = """
          해당 일정의 출석 목록을 조회합니다.

          **출석 상태 (attendanceStatus):**
          - `PRESENT`: 출석
          - `ABSENT`: 결석
          - `UNDECIDED`: 미정 (아직 출석 안 함)

          **권한:** 스터디 멤버만 가능
          """
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "출석 목록 조회 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GetAttendanceListResponse.class),
              examples = @ExampleObject(value = """
                  {
                    "isSuccess": true,
                    "code": "200",
                    "message": "OK",
                    "result": {
                      "attendances": [
                        {
                          "member": {
                            "memberId": 1,
                            "memberName": "홍길동",
                            "memberProfileImageUrl": "https://example.com/profile1.jpg"
                          },
                          "attendanceStatus": "PRESENT",
                          "attendedAt": "2024-01-15T14:30:00"
                        },
                        {
                          "member": {
                            "memberId": 2,
                            "memberName": "김철수",
                            "memberProfileImageUrl": "https://example.com/profile2.jpg"
                          },
                          "attendanceStatus": "UNDECIDED",
                          "attendedAt": null
                        }
                      ],
                      "totalCount": 2
                    }
                  }
                  """)
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "스터디 멤버가 아님 (STUDY4032)"
      )
  })
  @GetMapping
  public ResponseEntity<ApiResponse<GetAttendanceListResponse>> getAttendanceList(
      @Parameter(description = "스터디 ID", required = true, example = "1") @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true, example = "100") @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    GetAttendanceListResponse response = getAttendanceService.getAttendanceList(studyId,
        scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }

  @Operation(
      summary = "출석체크 QR 코드 조회",
      description = """
          출석체크용 QR 코드 이미지 URL을 조회합니다.

          **응답 필드:**
          - `attendanceActive`: 출석체크 진행 중 여부
          - `qrCodeImageUrl`: QR 코드 이미지 URL (출석체크 미시작 시 null)

          **QR 코드 내용:**
          - QR 코드에는 암호화된 토큰 문자열만 담겨 있음
          - 멤버가 QR 스캔 후 토큰을 `/check` API에 전달하여 출석 처리

          **권한:** 스터디 멤버만 가능
          """
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "QR 코드 조회 성공",
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = GetAttendanceInfoResponse.class),
              examples = {
                  @ExampleObject(name = "출석체크 진행 중", value = """
                      {
                        "isSuccess": true,
                        "code": "200",
                        "message": "OK",
                        "result": {
                          "attendanceActive": true,
                          "qrCodeImageUrl": "https://s3.amazonaws.com/.../qr-code.png"
                        }
                      }
                      """),
                  @ExampleObject(name = "출석체크 미시작", value = """
                      {
                        "isSuccess": true,
                        "code": "200",
                        "message": "OK",
                        "result": {
                          "attendanceActive": false,
                          "qrCodeImageUrl": null
                        }
                      }
                      """)
              }
          )
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "스터디 멤버가 아님 (STUDY4032)"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "일정을 찾을 수 없음 (SCHEDULE404)"
      )
  })
  @GetMapping("/qr")
  public ResponseEntity<ApiResponse<GetAttendanceInfoResponse>> getAttendanceQrCode(
      @Parameter(description = "스터디 ID", required = true, example = "1") @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true, example = "100") @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    GetAttendanceInfoResponse response = getAttendanceService.getAttendanceQrCode(studyId,
        scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
