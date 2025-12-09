package kr.spot.common;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import kr.spot.ports.dto.MemberInfoResponse;

public class MemberInfoResponseFixture {

  public static final String NAME = "홍길동";
  public static final String PROFILE_IMAGE_URL = "http://example.com/profile.png";

  public static MemberInfoResponse create() {
    return MemberInfoResponse.of(NAME, PROFILE_IMAGE_URL);
  }

  public static MemberInfoResponse create(String name, String profileImageUrl) {
    return MemberInfoResponse.of(name, profileImageUrl);
  }

  public static MemberInfoResponse create(Long memberId) {
    return MemberInfoResponse.of(NAME + memberId, PROFILE_IMAGE_URL);
  }

  public static Map<Long, MemberInfoResponse> createMap(List<Long> memberIds) {
    return memberIds.stream()
        .collect(Collectors.toMap(
            Function.identity(),
            MemberInfoResponseFixture::create
        ));
  }
}