package kr.spot.study.presentation.command.dto.request;

import java.util.Set;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.Style;

public record CreateStudyRequest(
    String name,
    Integer maxMembers,
    Boolean hasFee,
    Integer amount,
    String description,
    Boolean isOnline,
    Set<Category> categories,
    Set<Style> styles,
    Set<String> regionCodes
) {

}
