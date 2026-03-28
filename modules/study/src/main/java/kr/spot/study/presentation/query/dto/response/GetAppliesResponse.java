package kr.spot.study.presentation.query.dto.response;

import java.util.List;

public record GetAppliesResponse(List<Apply> applies) {

  public static GetAppliesResponse of(List<Apply> applies) {
    return new GetAppliesResponse(applies);
  }

  public record Apply(
      Long applicantId,
      Long memberId,
      String nickname,
      String description,
      String profileImageUrl
  ) {

    public static Apply of(Long applicantId, Long memberId, String nickname, String description,
        String profileImageUrl) {
      return new Apply(applicantId, memberId, nickname, description, profileImageUrl);
    }
  }
}
