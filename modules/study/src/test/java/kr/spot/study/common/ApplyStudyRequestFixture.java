package kr.spot.study.common;

import kr.spot.study.presentation.command.dto.request.ApplyStudyRequest;

public class ApplyStudyRequestFixture {

  public static final String DEFAULT_MESSAGE = "스터디에 참여하고 싶습니다.";

  public static ApplyStudyRequest create() {
    return new ApplyStudyRequest(DEFAULT_MESSAGE);
  }

  public static ApplyStudyRequest withMessage(String message) {
    return new ApplyStudyRequest(message);
  }
}
