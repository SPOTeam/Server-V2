package kr.spot.schedule.presentation.command;


import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.code.status.SuccessStatus;
import kr.spot.schedule.application.command.ManageScheduleService;
import kr.spot.schedule.presentation.command.dto.CreateScheduleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "스터디 일정")
@RestController
@RequestMapping("/api/studies/{studyId}/schedules")
@RequiredArgsConstructor
public class ScheduleCommandController {

  private final ManageScheduleService manageScheduleService;

  @PostMapping
  public ResponseEntity<ApiResponse<Void>> createSchedule(
      @PathVariable Long studyId,
      @RequestBody CreateScheduleRequest request) {
    manageScheduleService.createSchedule(request, studyId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._CREATED));
  }

  @DeleteMapping("/{scheduleId}")
  public ResponseEntity<ApiResponse<Void>> deleteSchedule(
      @PathVariable Long studyId,
      @PathVariable Long scheduleId) {
    manageScheduleService.deleteSchedule(studyId, scheduleId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
