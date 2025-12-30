package kr.spot.event;

public record StudyApplicationProcessedEvent(
    Long studyId,
    Long applicantId,
    Long processedBy,
    String decision,
    String studyName,
    String studyThumbnailUrl
) {

  public static StudyApplicationProcessedEvent of(
      Long studyId,
      Long applicantId,
      Long processedBy,
      String decision,
      String studyName,
      String studyThumbnailUrl
  ) {
    return new StudyApplicationProcessedEvent(
        studyId, applicantId, processedBy, decision, studyName, studyThumbnailUrl);
  }

  public boolean isApproved() {
    return "APPROVE".equals(decision);
  }
}
