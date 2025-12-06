package kr.spot.presentation.query.dto.request;

import java.util.List;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.FeeCategory;
import kr.spot.domain.enums.RecruitingStatus;

public record StudySearchRequestForEqual(
    FeeCategory feeCategory,
    RecruitingStatus recruitingStatus,
    List<Category> categories,
    Long cursor,
    int limit
) {

}
