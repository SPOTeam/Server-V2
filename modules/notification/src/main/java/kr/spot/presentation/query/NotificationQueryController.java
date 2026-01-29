package kr.spot.presentation.query;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.spot.ApiResponse;
import kr.spot.annotations.CurrentMember;
import kr.spot.application.query.GetNotificationService;
import kr.spot.code.status.SuccessStatus;
import kr.spot.presentation.query.dto.GetNotificationListResponse;
import kr.spot.presentation.query.dto.GetUnreadNotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "알림")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationQueryController {

  private final GetNotificationService getNotificationService;

  @Operation(summary = "내 알림 목록 조회", description = "로그인한 사용자의 알림 목록을 조회합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자입니다.",
          content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<GetNotificationListResponse>> getMyNotifications(
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    GetNotificationListResponse response = getNotificationService.getMyNotifications(memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }

  @Operation(summary = "읽지 않은 알림 존재 여부 조회", description = "로그인한 사용자의 읽지 않은 알림이 있는지 여부를 조회합니다.")
  @GetMapping("/me/unread")
  public ResponseEntity<ApiResponse<GetUnreadNotificationResponse>> hasUnreadNotifications(
      @CurrentMember @Parameter(hidden = true) Long memberId) {
    GetUnreadNotificationResponse response = getNotificationService.hasUnreadNotifications(
        memberId);
    return ResponseEntity.ok(ApiResponse.onSuccess(SuccessStatus._OK, response));
  }
}
