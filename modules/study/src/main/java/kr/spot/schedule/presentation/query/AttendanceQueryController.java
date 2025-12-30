package kr.spot.schedule.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "스터디 일정 - 출석체크")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules/{scheduleId}/attendance")
@RequiredArgsConstructor
public class AttendanceQueryController {

  private final GetAttendanceService getAttendanceService;

  @Operation(summary = "출석 목록 조회", description = "해당 일정의 출석 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<GetAttendanceListResponse>> getAttendanceList(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    GetAttendanceListResponse response = getAttendanceService.getAttendanceList(studyId,
        scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }

  @Operation(summary = "출석체크 QR 코드 조회", description = "출석체크용 QR 코드 이미지 URL을 조회합니다.")
  @GetMapping("/qr")
  public ResponseEntity<ApiResponse<GetAttendanceInfoResponse>> getAttendanceQrCode(
      @Parameter(description = "스터디 ID", required = true) @PathVariable Long studyId,
      @Parameter(description = "일정 ID", required = true) @PathVariable Long scheduleId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    GetAttendanceInfoResponse response = getAttendanceService.getAttendanceQrCode(studyId,
        scheduleId, memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
