package kr.spot.schedule.presentation.query.dto;

import java.time.LocalDateTime;
import java.util.List;

public record GetAttendanceListResponse(
    List<AttendanceInfoResponse> attendances,
    int totalCount
) {

  public static GetAttendanceListResponse from(List<AttendanceInfoResponse> attendances,
      int totalCount) {
    return new GetAttendanceListResponse(attendances, totalCount);
  }

  public record AttendanceInfoResponse(
      MemberInfoResponse member,
      String attendanceStatus,
      LocalDateTime attendedAt
  ) {

    public static AttendanceInfoResponse from(MemberInfoResponse member, String attendanceStatus,
        LocalDateTime attendedAt) {
      return new AttendanceInfoResponse(member, attendanceStatus, attendedAt);
    }
  }

  public record MemberInfoResponse(
      Long memberId,
      String memberName,
      String memberProfileImageUrl
  ) {

    public static MemberInfoResponse from(Long memberId, String memberName,
        String memberProfileImageUrl) {
      return new MemberInfoResponse(memberId, memberName, memberProfileImageUrl);
    }
  }
}
