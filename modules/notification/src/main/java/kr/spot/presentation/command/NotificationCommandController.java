package kr.spot.presentation.command;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.command.NotificationCommandService;
import kr.spot.code.status.SuccessStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationCommandController {

  private final NotificationCommandService notificationCommandService;

  @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
  @PostMapping("/{notificationId}/read")
  public ResponseEntity<ApiResponse<Void>> markAsRead(
      @PathVariable Long notificationId,
      @CurrentMember @Parameter(hidden = true) Long memberId
  ) {
    notificationCommandService.markAllAsRead(memberId, notificationId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK));
  }
}
