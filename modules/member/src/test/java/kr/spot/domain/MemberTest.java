package kr.spot.domain;

import static kr.spot.common.fixture.MemberFixture.ID;
import static kr.spot.common.fixture.MemberFixture.NAME;
import static kr.spot.common.fixture.MemberFixture.PROFILE_IMAGE;
import static kr.spot.common.fixture.MemberFixture.email;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kr.spot.domain.enums.LoginType;
import kr.spot.domain.vo.Email;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberTest {

  @Test
  @DisplayName("정상적으로 회원을 생성할 수 있다.")
  void should_create_member_successfully() {
    // given
    Email email = email();

    // when
    Member member = Member.of(ID, email, NAME, LoginType.KAKAO, PROFILE_IMAGE);

    // then
    assertAll(
        () -> assertNotNull(member),
        () -> assertEquals(email, member.getEmail()),
        () -> assertEquals(NAME, member.getName())
    );
  }

  @Test
  @DisplayName("이름이 null 이거나 공백일 경우 회원 생성에 실패한다.")
  void should_fail_to_create_member_when_name_is_null_or_empty() {
    // given
    Email email = email();

    // when & then
    assertThrows(GeneralException.class,
        () -> Member.of(ID, email, null, LoginType.KAKAO, PROFILE_IMAGE));
    assertThrows(GeneralException.class,
        () -> Member.of(ID, email, "", LoginType.KAKAO, PROFILE_IMAGE));
    assertThrows(GeneralException.class,
        () -> Member.of(ID, email, "   ", LoginType.KAKAO, PROFILE_IMAGE));
  }

  @Test
  @DisplayName("이메일이 null 이거나 공백일 경우 회원 생성에 실패한다.")
  void should_fail_to_create_member_when_email_is_null_or_empty() {
    // when & then
    assertThrows(GeneralException.class,
        () -> Member.of(ID, null, NAME, LoginType.KAKAO, PROFILE_IMAGE));
  }
}
