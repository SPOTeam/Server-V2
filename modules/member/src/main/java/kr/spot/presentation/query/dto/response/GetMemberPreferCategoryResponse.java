package kr.spot.presentation.query.dto.response;

import java.util.List;

public record GetMemberPreferCategoryResponse(
    List<String> categories,
    long totalCount
) {

  public static GetMemberPreferCategoryResponse from(List<String> categories,
      long totalCount) {
    return new GetMemberPreferCategoryResponse(categories, totalCount);
  }
}
