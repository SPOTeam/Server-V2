package kr.spot.post.presentation.command.dto;

public record ManagePostRequest(
    String title, String content, Boolean isPrivate) {

}
