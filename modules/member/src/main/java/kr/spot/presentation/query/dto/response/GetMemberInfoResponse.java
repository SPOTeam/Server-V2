package kr.spot.presentation.query.dto.response;

import kr.spot.domain.enums.LoginType;

public record GetMemberInfoResponse(
    Long memberId,
    String nickname,
    String profileImageUrl,
    LoginType loginType,
    String email,
    StudyParticipationInfoResponse studyParticipationInfo
) {

  public static GetMemberInfoResponse from(
      long memberId,
      String nickname,
      String profileImageUrl,
      LoginType loginType,
      String email,
      StudyParticipationInfoResponse studyParticipationInfo
  ) {
    return new GetMemberInfoResponse(
        memberId,
        nickname,
        profileImageUrl,
        loginType,
        email,
        studyParticipationInfo
    );
  }

  public record StudyParticipationInfoResponse(
      long participatingStudyCount,
      long recruitingStudyCount,
      long appliedStudyCount
  ) {

    public static StudyParticipationInfoResponse from(
        long participatingStudyCount,
        long recruitingStudyCount,
        long appliedStudyCount
    ) {
      return new StudyParticipationInfoResponse(
          participatingStudyCount,
          recruitingStudyCount,
          appliedStudyCount
      );
    }

  }

}
