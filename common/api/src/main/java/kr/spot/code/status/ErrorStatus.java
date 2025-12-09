package kr.spot.code.status;

import kr.spot.code.BaseErrorCode;
import kr.spot.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

  //공통 에러
  _INTERNAL_SERVER_ERROR(500, "COMMON500", "서버 내부 오류 발생"),
  _BAD_REQUEST(404, "COMMON4000", "잘못된 요청입니다."),
  _UNAUTHORIZED(401, "COMMON4001", "인증되지 않은 요청입니다."),
  _FORBIDDEN(403, "COMMON4002", "접근이 거부되었습니다."),
  _FAIL_TO_UPLOAD_FILE(400, "COMMON4003", "파일 업로드에 실패했습니다."),

  // JWT 관련
  _EMPTY_JWT(400, "COMMON4005", "JWT 토큰이 비어있습니다."),
  _INVALID_JWT(400, "COMMON4006", "유효하지 않은 JWT token입니다."),
  _EXPIRED_JWT(400, "COMMON4007", "만료된 JWT token입니다."),
  _UNSUPPORTED_JWT(400, "COMMON4008", "지원되지 않는 JWT token입니다."),
  _NO_AUTHORIZED(401, "COMMON4009", "권한이 없습니다."),
  _INVALID_REFRESH_TOKEN(400, "COMMON4010", "유효하지 않은 리프레시 토큰입니다."),

  // 회원 관련
  _MEMBER_NOT_FOUND(404, "MEMBER404", "회원을 찾을 수 없습니다."),
  _INVALID_EMAIL_FORMAT(400, "MEMBER4000", "유효하지 않은 이메일 형식입니다."),
  _NAME_CAN_NOT_NULL_OR_EMPTY(400, "MEMBER4001", "이름은 null 또는 공백일 수 없습니다."),
  _EMAIL_CAN_NOT_NULL_OR_EMPTY(400, "MEMBER4002", "이메일은 null 또는 공백일 수 없습니다."),
  _MEMBER_UNSUPPORTED_LOGIN_TYPE(400, "MEMBER4003", "지원하지 않는 로그인 타입입니다."),
  _MEMBER_EMAIL_EXIST(400, "MEMBER4004", "이미 해당 방식으로 가입 내역이 존재하는 이메일입니다."),
  _FAIL_TO_UPDATE_NAME(400, "MEMBER4005", "이름 변경에 실패했습니다."),

  // 스터디 관련
  _MAX_MEMBERS_MUST_BE_POSITIVE(400, "STUDY4000", "최대 인원 수는 양수여야 합니다."),
  _INVALID_FEE_AMOUNT(400, "STUDY4000", "유효하지 않은 스터디 비용입니다."),
  _NO_SUCH_CATEGORY(400, "STUDY4001", "존재하지 않는 카테고리입니다."),
  _NO_SUCH_STUDY_MEMBER_STATUS(400, "STUDY4002", "존재하지 않는 스터디 멤버 상태입니다."),
  _STUDY_NOT_FOUND(404, "STUDY404", "스터디를 찾을 수 없습니다."),

  // 게시글 관련
  _POST_NOT_FOUND(404, "POST404", "게시글을 찾을 수 없습니다."),
  _COMMENT_NOT_FOUND(404, "COMMENT404", "댓글을 찾을 수 없습니다."),
  _ONLY_AUTHOR_CAN_MODIFY(403, "POST403", "게시글 및 댓글 수정은 작성자만 가능합니다."),
  _ALREADY_LIKED(400, "POST4000", "이미 좋아요를 누른 게시글입니다."),
  _ALREADY_UNLIKED(400, "POST4001", "좋아요를 누르지 않은 게시글입니다."),
  _SORT_TYPE_INVALID(400, "POST4002", "유효하지 않은 정렬 타입입니다."),

  // 지역 관련
  _REGION_NOT_FOUND(404, "REGION404", "지역을 찾을 수 없습니다."),
  _NO_SUCH_REGION(400, "REGION4000", "존재하지 않는 지역 코드입니다."),
  ;

  private final int httpStatus;
  private final String code;
  private final String message;

  @Override
  public ErrorReasonDTO getReason() {
    return ErrorReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .build()
        ;
  }

  @Override
  public ErrorReasonDTO getReasonHttpStatus() {
    return ErrorReasonDTO.builder()
        .message(message)
        .code(code)
        .isSuccess(false)
        .httpStatus(httpStatus)
        .build()
        ;
  }
}
