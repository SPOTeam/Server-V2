package kr.spot.review.domain.vo;

import static kr.spot.review.common.ReviewFixture.MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.OTHER_MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class WriterInfoTest {

  @Nested
  @DisplayName("작성자 검증 (validateIsOwnMember)")
  class ValidateIsOwnMember {

    @Test
    @DisplayName("본인인 경우 예외가 발생하지 않는다")
    void should_not_throw_when_owner() {
      // given
      WriterInfo writerInfo = writerInfo();

      // when & then (예외 발생 안함)
      writerInfo.validateIsOwnMember(MEMBER_ID);
    }

    @Test
    @DisplayName("본인이 아닌 경우 예외가 발생한다")
    void should_throw_when_not_owner() {
      // given
      WriterInfo writerInfo = writerInfo();

      // when & then
      assertThatThrownBy(() -> writerInfo.validateIsOwnMember(OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
    }
  }

  @Nested
  @DisplayName("동일 작성자 확인 (isSameWriter)")
  class IsSameWriter {

    @Test
    @DisplayName("동일한 작성자인 경우 true를 반환한다")
    void should_return_true_when_same_writer() {
      // given
      WriterInfo writerInfo = writerInfo();

      // when
      boolean result = writerInfo.isSameWriter(MEMBER_ID);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("다른 작성자인 경우 false를 반환한다")
    void should_return_false_when_different_writer() {
      // given
      WriterInfo writerInfo = writerInfo();

      // when
      boolean result = writerInfo.isSameWriter(OTHER_MEMBER_ID);

      // then
      assertThat(result).isFalse();
    }
  }
}
