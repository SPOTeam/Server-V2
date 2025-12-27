package kr.spot.study.domain.enums;

public enum FeeCategory {

  NONE,
  BELOW_10K,
  FROM_10K_TO_20K,
  FROM_20K_TO_30K,
  FROM_30K_TO_40K,
  FROM_40K_TO_50K,
  ABOVE_50K;

  public static FeeCategory getFeeCategory(Integer amount) {
    if (amount == null || amount == 0) {
      return NONE;
    } else if (amount > 0 && amount < 10000) {
      return BELOW_10K;
    } else if (amount >= 10000 && amount < 20000) {
      return FROM_10K_TO_20K;
    } else if (amount >= 20000 && amount < 30000) {
      return FROM_20K_TO_30K;
    } else if (amount >= 30000 && amount < 40000) {
      return FROM_30K_TO_40K;
    } else if (amount >= 40000 && amount < 50000) {
      return FROM_40K_TO_50K;
    } else {
      return ABOVE_50K;
    }
  }
}
