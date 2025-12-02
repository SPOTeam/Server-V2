package kr.spot.common;

import kr.spot.ports.dto.WriterInfoResponse;

public abstract class WriterInfoFixture {

  public static WriterInfoResponse response(Long writerId, String nickname, String img) {
    return new WriterInfoResponse(writerId, nickname, img);
  }
}
