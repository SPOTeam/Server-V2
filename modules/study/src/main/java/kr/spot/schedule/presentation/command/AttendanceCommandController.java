package kr.spot.schedule.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "스터디 일정 - 출석체크")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules/{scheduleId}/attendance")
@RequiredArgsConstructor
public class AttendanceCommandController {

  private final AttendanceCommandService attendanceCommandService;

  @Operation(summary = "출석체크 시작", description = "스터디장이 출석체크를 시작합니다. QR 코드 이미지는 비동기로 생성됩니다.")
  @PostMapping
  public ResponseEntity<ApiResponse<Void>> startAttendanceCheck(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.startAttendance(studyId, scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "출석체크 종료", description = "스터디장이 출석체크를 종료합니다.")
  @DeleteMapping
  public ResponseEntity<ApiResponse<Void>> stopAttendanceCheck(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.stopAttendance(studyId, scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }

  @Operation(summary = "출석체크 처리", description = "QR 코드를 통해 출석체크를 처리합니다.")
  @PostMapping("/check")
  public ResponseEntity<ApiResponse<Void>> checkAttendance(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
      @Parameter(description = "암호화된 출석 토큰", required = true) @RequestParam String token,
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    attendanceCommandService.checkAttendance(token, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
