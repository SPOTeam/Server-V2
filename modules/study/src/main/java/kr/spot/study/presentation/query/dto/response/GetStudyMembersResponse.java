package kr.spot.study.presentation.query.dto.response;

import java.util.List;

public record GetStudyMembersResponse(
    List<MemberResponse> members,
    long totalMembers
) {

  public static GetStudyMembersResponse of(
      List<MemberResponse> members,
      long totalMembers
  ) {
    return new GetStudyMembersResponse(members, totalMembers);
  }

  public record MemberResponse(
      Long memberId,
      String nickname,
      String profileImageUrl,
      boolean isOwner
  ) {

    public static MemberResponse of(
        Long memberId,
        String nickname,
        String profileImageUrl,
        boolean isOwner
    ) {
      return new MemberResponse(
          memberId,
          nickname,
          profileImageUrl,
          isOwner
      );
    }

  }

}
