package kr.spot.study.presentation.command.dto.response;

public record CreateStudyResponse(Long studyId) {

  public static CreateStudyResponse from(long studyId) {
    return new CreateStudyResponse(studyId);
  }
}
