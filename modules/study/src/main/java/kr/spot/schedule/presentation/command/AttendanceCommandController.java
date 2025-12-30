package kr.spot.schedule.presentation.command;

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
import kr.spot.schedule.application.command.AttendanceCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정 - 출석체크", description = "스터디 일정 출석체크 관련 API")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules/{scheduleId}/attendance")
@RequiredArgsConstructor
public class AttendanceCommandController {

  private final AttendanceCommandService attendanceCommandService;

  @Operation(
      summary = "출석체크 시작",
      description = """
          스터디장이 출석체크를 시작합니다.

          **동작:**
          - 모든 스터디 멤버(OWNER, APPROVED)에 대해 출석 기록 생성
          - 스터디장(OWNER)은 자동으로 출석(PRESENT) 처리
          - 일반 멤버는 미정(UNDECIDED) 상태로 생성
          - QR 코드 이미지는 비동기로 생성되며, `/qr` 엔드포인트에서 조회 가능

          **권한:** 스터디장만 가능
          """
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "출석체크 시작 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "일정 시간이 아님 (ATTENDANCE4000) / 이미 출석체크 진행 중 (SCHEDULE4000)",
          content = @Content(examples = {
              @ExampleObject(name = "일정 시간 외", value = """
                  {"isSuccess": false, "code": "ATTENDANCE4000", "message": "일정 시간 외에는 출석체크를 진행할 수 없습니다."}
                  """),
              @ExampleObject(name = "이미 진행 중", value = """
                  {"isSuccess": false, "code": "SCHEDULE4000", "message": "이미 출석 QR 코드가 할당된 일정입니다."}
                  """)
          })
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "스터디장이 아님 (STUDY403)",
          content = @Content(examples = @ExampleObject(value = """
              {"isSuccess": false, "code": "STUDY403", "message": "스터디장만 접근 가능합니다."}
              """))
      )
  })
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> startAttendanceCheck(
      @Parameter(description = "스터디 ID", required = true, example = "1") @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true, example = "100") @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.startAttendance(studyId, scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(
      summary = "출석체크 종료",
      description = """
          스터디장이 출석체크를 종료합니다.

          **동작:**
          - 출석체크 비활성화
          - QR 코드 이미지 URL 삭제

          **권한:** 스터디장만 가능
          """
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "출석체크 종료 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "스터디장이 아님 (STUDY403)"
      )
  })
  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> stopAttendanceCheck(
      @Parameter(description = "스터디 ID", required = true, example = "1") @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true, example = "100") @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.stopAttendance(studyId, scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(
      summary = "출석체크 처리 (QR 스캔)",
      description = """
          QR 코드를 스캔하여 출석체크를 처리합니다.

          **동작:**
          - 암호화된 토큰을 복호화하여 studyId, scheduleId 추출
          - 해당 멤버의 출석 상태를 PRESENT로 변경

          **QR 코드 토큰:**
          - QR 코드에는 암호화된 토큰 문자열만 담겨 있음
          - 앱에서 QR 스캔 후 해당 토큰을 이 API의 token 파라미터로 전달

          **권한:** 스터디 멤버만 가능
          """
  )
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "200",
          description = "출석 처리 성공"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "400",
          description = "유효하지 않은 토큰 (ATTENDANCE4003) / 출석체크 미시작 (ATTENDANCE4001) / 일정 시간 외 (ATTENDANCE4000)",
          content = @Content(examples = {
              @ExampleObject(name = "유효하지 않은 토큰", value = """
                  {"isSuccess": false, "code": "ATTENDANCE4003", "message": "유효하지 않은 출석 토큰입니다."}
                  """),
              @ExampleObject(name = "출석체크 미시작", value = """
                  {"isSuccess": false, "code": "ATTENDANCE4001", "message": "출석체크가 시작되지 않았습니다."}
                  """)
          })
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "403",
          description = "스터디 멤버가 아님 (STUDY4032)"
      ),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(
          responseCode = "404",
          description = "출석 기록 없음 (STUDYMEMBER404)"
      )
  })
  @PostMapping("/check")
  public ResponseEntity<ApiResponse<Void>> checkAttendance(
      @Parameter(description = "스터디 ID", required = true, example = "1") @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true, example = "100") @PathVariable Long scheduleId,
      @Parameter(
          description = "QR 코드에서 읽은 암호화된 출석 토큰",
          required = true,
          example = "dK8x2mP9qL3nR7vT..."
      ) @RequestParam String token,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.checkAttendance(token, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
