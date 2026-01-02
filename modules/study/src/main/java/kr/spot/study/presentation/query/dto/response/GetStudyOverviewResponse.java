package kr.spot.study.presentation.query.dto.response;

import java.util.List;

public record GetStudyOverviewResponse(
    List<StudyOverview> content,
    boolean hasNext,
    Long nextCursor,
    long totalElements
) {

  public static GetStudyOverviewResponse of(
      List<StudyOverview> content,
      boolean hasNext,
      Long nextCursor,
      long totalElements
  ) {
    return new GetStudyOverviewResponse(content, hasNext, nextCursor, totalElements);
  }

  public record StudyOverview(
      Long id,
      String name,
      String description,
      int maxMembers,
      int currentMembers,
      long likeCount,
      boolean isLiked,
      long hitCount,
      String profileImageUrl
  ) {

    public static StudyOverview of(
        Long id,
        String title,
        String description,
        int totalMembers,
        int currentMembers,
        long likeCount,
        boolean isLiked,
        long hitCount,
        String profileImageUrl
    ) {
      return new StudyOverview(
          id,
          title,
          description,
          totalMembers,
          currentMembers,
          likeCount,
          isLiked,
          hitCount,
          profileImageUrl
      );
    }
  }
}
