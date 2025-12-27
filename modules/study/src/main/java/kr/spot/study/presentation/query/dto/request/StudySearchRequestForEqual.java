package kr.spot.study.presentation.query.dto.request;

import java.util.List;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.FeeCategory;
import kr.spot.study.domain.enums.RecruitingStatus;

public record StudySearchRequestForEqual(
    FeeCategory feeCategory,
    RecruitingStatus recruitingStatus,
    List<Category> categories,
    Long cursor,
    int limit
) {

}
