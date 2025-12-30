package kr.spot.schedule.application.event;

public record AttendanceStartedEvent(
    long studyId,
    long scheduleId,
    String qrContent
) {

  public static AttendanceStartedEvent of(long studyId, long scheduleId, String qrContent) {
    return new AttendanceStartedEvent(studyId, scheduleId, qrContent);
  }
}
