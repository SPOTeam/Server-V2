package kr.spot.study.presentation.query.dto.response;

import java.util.List;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.Style;

public record GetStudyInfoResponse(
    Long id,
    String title,
    String description,
    String thumbnailUrl,
    int maxMembers,
    boolean hasFee,
    Integer amount,
    List<Category> categories,
    List<Style> styles,
    List<String> regionCodes,
    boolean isOnline,
    boolean isLiked,
    Statistics statistics,
    String viewerStatus
) {

  public static GetStudyInfoResponse of(
      Long id,
      String title,
      String description,
      String thumbnailUrl,
      int maxMembers,
      boolean hasFee,
      Integer amount,
      List<Category> categories,
      List<Style> styles,
      List<String> regionCodes,
      boolean isOnline,
      boolean isLiked,
      Statistics statistics,
      String viewerStatus
  ) {
    return new GetStudyInfoResponse(
        id,
        title,
        description,
        thumbnailUrl,
        maxMembers,
        hasFee,
        amount,
        categories,
        styles,
        regionCodes,
        isOnline,
        isLiked,
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
