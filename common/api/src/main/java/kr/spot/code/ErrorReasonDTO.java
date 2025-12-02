package kr.spot.code;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorReasonDTO {

  private final boolean isSuccess;
  private final String code;
  private final String message;

  private int httpStatus;
}
