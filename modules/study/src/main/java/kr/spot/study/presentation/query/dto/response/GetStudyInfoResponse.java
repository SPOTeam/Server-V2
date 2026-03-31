package kr.spot.study.presentation.query.dto.response;

import java.util.List;
import kr.spot.study.domain.enums.Category;

public record GetStudyInfoResponse(
    Long id,
    String title,
    String description,
    String thumbnailUrl,
    List<Category> categories,
    Statistics statistics,
    String viewerStatus
) {

  public static GetStudyInfoResponse of(
      Long id,
      String title,
      String description,
      String thumbnailUrl,
      List<Category> categories,
      Statistics statistics,
      String viewerStatus
  ) {
    return new GetStudyInfoResponse(
        id,
        title,
        description,
        thumbnailUrl,
        categories,
        statistics,
        viewerStatus
    );
  }

  public record Statistics(
      int totalMembers,
      int currentMembers,
      long likeCount,
      long hitCount
  ) {

    public static Statistics of(
        int totalMembers,
        int currentMembers,
        long likeCount,
        long hitCount
    ) {
      return new Statistics(
          totalMembers,
          currentMembers,
          likeCount,
          hitCount
      );
    }
  }

}
