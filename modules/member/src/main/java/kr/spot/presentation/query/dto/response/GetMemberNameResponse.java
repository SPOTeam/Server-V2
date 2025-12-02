package kr.spot.presentation.query.dto.response;

public record GetMemberNameResponse(
    String name
) {

  public static GetMemberNameResponse from(String name) {
    return new GetMemberNameResponse(name);
  }
}
