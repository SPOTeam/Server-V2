package kr.spot.ports.dto;

public record StudyParticipationInfo(
    long participatingStudyCount,
    long recruitingStudyCount,
    long appliedStudyCount
) {

  public static StudyParticipationInfo from(
      long participatingStudyCount,
      long recruitingStudyCount,
      long appliedStudyCount
  ) {
    return new StudyParticipationInfo(
        participatingStudyCount,
        recruitingStudyCount,
        appliedStudyCount
    );
  }

}