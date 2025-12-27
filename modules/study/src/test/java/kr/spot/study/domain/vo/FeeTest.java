package kr.spot.study.domain.vo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeeTest {

  @Test
  @DisplayName("hasFee=false이면 amount는 0으로 고정된다")
  void should_set_amount_zero_when_has_fee_false() {
    Fee fee = Fee.of(false, 1000); // amount 값은 무시됨
    assertThat(fee.isHasFee()).isFalse();
    assertThat(fee.getAmount()).isZero();
  }

  @Test
  @DisplayName("hasFee=true이고 유효한 금액이면 그대로 생성된다")
  void should_create_fee_when_valid_amount_and_has_fee_true() {
    Fee fee = Fee.of(true, 5000);
    assertThat(fee.isHasFee()).isTrue();
    assertThat(fee.getAmount()).isEqualTo(5000);
  }

  @Test
  @DisplayName("hasFee=true인데 amount가 null이면 예외 발생")
  void should_throw_exception_when_amount_is_null_and_has_fee_true() {
    assertThatThrownBy(() -> Fee.of(true, null))
        .isInstanceOf(GeneralException.class)
        .hasMessageContaining(ErrorStatus._INVALID_FEE_AMOUNT.getCode());
  }

  @Test
  @DisplayName("hasFee=true인데 amount가 음수면 예외 발생")
  void should_throw_exception_when_amount_is_negative_and_has_fee_true() {
    assertThatThrownBy(() -> Fee.of(true, -100))
        .isInstanceOf(GeneralException.class)
        .hasMessageContaining(ErrorStatus._INVALID_FEE_AMOUNT.getCode());
  }
}
