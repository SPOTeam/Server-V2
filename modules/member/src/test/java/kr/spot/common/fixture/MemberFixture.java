package kr.spot.common.fixture;


import kr.spot.domain.Member;
import kr.spot.domain.enums.LoginType;
import kr.spot.domain.vo.Email;

public class MemberFixture {

  public static final String EMAIL = "example@email.com";
  public static final String NAME = "스팟";
  public static final String PROFILE_IMAGE = "profile_image.url";

  public static final String INVALID_EMAIL = "invalid-email";
  public static final long ID = 1L;

  public static Email email() {
    return Email.of(EMAIL);
  }

  public static String invalidEmail() {
    return INVALID_EMAIL;
  }

  public static Member member() {
    return Member.of(ID, email(), NAME, LoginType.KAKAO, PROFILE_IMAGE);
  }
}
