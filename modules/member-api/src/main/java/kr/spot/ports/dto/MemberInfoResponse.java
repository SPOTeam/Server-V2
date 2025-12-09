package kr.spot.ports.dto;

public record MemberInfoResponse(
    String name,
    String profileImageUrl
) {

  public static MemberInfoResponse of(String name, String profileImageUrl) {
    return new MemberInfoResponse(name, profileImageUrl);
  }
}
