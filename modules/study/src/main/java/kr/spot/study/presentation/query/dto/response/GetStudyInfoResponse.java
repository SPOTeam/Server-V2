package kr.spot.study.presentation.query.dto.response;

import java.util.List;
import kr.spot.study.domain.enums.Category;

public record GetStudyInfoResponse(
    String title,
    String description,
    String thumbnailUrl,
    List<Category> categories,
    Statistics statistics
) {

  public static GetStudyInfoResponse of(
      String title,
      String description,
      String thumbnailUrl,
      List<Category> categories,
      Statistics statistics
  ) {
    return new GetStudyInfoResponse(
        title,
        description,
        thumbnailUrl,
        categories,
        statistics
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
