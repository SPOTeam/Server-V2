package kr.spot.presentation.query.dto.response;

import java.util.List;

public record GetMemberPreferRegionResponse(
    List<String> regionCodes,
    long totalCount
) {

  public static GetMemberPreferRegionResponse from(List<String> regionCodes,
      long totalCount) {
    return new GetMemberPreferRegionResponse(regionCodes, totalCount);
  }
}
