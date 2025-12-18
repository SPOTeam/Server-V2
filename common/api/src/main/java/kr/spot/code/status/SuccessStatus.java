package kr.spot.code.status;

import kr.spot.code.BaseCode;
import kr.spot.code.ReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SuccessStatus implements BaseCode {
    
  _OK(200, "COMMON200", "OK"),
  _CREATED(201, "COMMON201", "생성 완료"),
  _ACCEPTED(202, "COMMON202", "요청 수락됨"),
  _NO_CONTENT(204, "COMMON204", "콘텐츠 없음"),
  ;

  private final Integer httpStatus;
  private final String code;
  private final String message;

  @Override
  public ReasonDTO getReason() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(true)
        .build();
  }

  @Override
  public ReasonDTO getReasonHttpStatus() {
    return ReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(true)
        .httpStatus(httpStatus)
        .build()
        ;
  }
}
