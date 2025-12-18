package kr.spot.presentation.command.dto.request;

import java.util.Set;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.Style;

public record CreateStudyRequest(
    String name,
    Integer maxMembers,
    Boolean hasFee,
    Integer amount,
    String description,
    Set<Category> categories,
    Set<Style> styles,
    Set<String> regionCodes
) {

}
