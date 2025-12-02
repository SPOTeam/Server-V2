package kr.spot.domain.vo;

import static kr.spot.common.fixture.MemberFixture.EMAIL;
import static kr.spot.common.fixture.MemberFixture.INVALID_EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  @DisplayName("정상적으로 이메일을 생성할 수 있다.")
  void should_create_email_successfully() {
    // given
    String emailStr = EMAIL;

    // when
    Email email = Email.of(emailStr);

    // then
    assertNotNull(email);
    assertEquals(emailStr, email.getValue());
  }

  @Test
  @DisplayName("잘못된 이메일 형식으로 인해 예외가 발생한다.")
  void should_throw_exception_for_invalid_email_format() {
    assertThrows(GeneralException.class, () -> {
      // when
      Email.of(INVALID_EMAIL);
    });
  }
}
