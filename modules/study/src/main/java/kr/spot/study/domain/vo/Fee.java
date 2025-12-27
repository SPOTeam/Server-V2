package kr.spot.study.domain.vo;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.enums.FeeCategory;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor(access = lombok.AccessLevel.PRIVATE)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Fee {

  private boolean hasFee;

  private Integer amount;

  @Enumerated(EnumType.STRING)
  private FeeCategory feeCategory;

  public static Fee of(boolean hasFee, Integer amount) {
    if (!hasFee) {
      amount = 0;
    } else {
      if (amount == null || amount < 0) {
        throw new GeneralException(ErrorStatus._INVALID_FEE_AMOUNT);
      }
    }
    return new Fee(hasFee, amount, FeeCategory.getFeeCategory(amount));
  }
}
