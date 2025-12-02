package kr.spot.presentation.command.dto.request;

import kr.spot.domain.enums.PostType;

public record ManagePostRequest(
    String title, String content, PostType postType
) {

}
