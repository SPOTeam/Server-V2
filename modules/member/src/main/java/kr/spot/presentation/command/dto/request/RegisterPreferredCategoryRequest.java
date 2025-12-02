package kr.spot.presentation.command.dto.request;

import java.util.List;

public record RegisterPreferredCategoryRequest(
    List<String> categories
) {

}
