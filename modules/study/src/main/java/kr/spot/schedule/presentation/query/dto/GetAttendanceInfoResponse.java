package kr.spot.schedule.presentation.query.dto;

public record GetAttendanceInfoResponse(
    boolean attendanceActive,
    String qrCodeImageUrl
) {

  public static GetAttendanceInfoResponse of(boolean attendanceActive, String qrCodeImageUrl) {
    return new GetAttendanceInfoResponse(attendanceActive, qrCodeImageUrl);
  }
}
