package kr.spot.review.domain;

import static kr.spot.review.common.ReviewFixture.ID;
import static kr.spot.review.common.ReviewFixture.STUDY_ID;
import static kr.spot.review.common.ReviewFixture.content;
import static kr.spot.review.common.ReviewFixture.privateReview;
import static kr.spot.review.common.ReviewFixture.review;
import static kr.spot.review.common.ReviewFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ReviewTest {

  @Nested
  @DisplayName("회고 생성 (of)")
  class CreateReview {

    @Test
    @DisplayName("회고 객체를 정상적으로 생성할 수 있다")
    void should_create_review_successfully() {
      // when
      Review review = Review.of(ID, STUDY_ID, writerInfo(), content(), false);

      // then
      assertThat(review).isNotNull();
      assertThat(review.getId()).isEqualTo(ID);
      assertThat(review.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(review.getWriterInfo()).isNotNull();
      assertThat(review.getContent()).isNotNull();
      assertThat(review.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("스터디원 전용 회고를 생성할 수 있다")
    void should_create_private_review_successfully() {
      // when
      Review review = Review.of(ID, STUDY_ID, writerInfo(), content(), true);

      // then
      assertThat(review.isPrivate()).isTrue();
    }

    @Test
    @DisplayName("isPrivate가 null이면 공개 회고로 간주한다")
    void should_treat_null_as_public() {
      // when
      Review review = Review.of(ID, STUDY_ID, writerInfo(), content(), null);

      // then
      assertThat(review.isPrivate()).isFalse();
    }
  }

  @Nested
  @DisplayName("공개 여부 확인 (isPrivate)")
  class IsPrivate {

    @Test
    @DisplayName("공개 회고는 false를 반환한다")
    void should_return_false_for_public_review() {
      // given
      Review review = review();

      // when & then
      assertThat(review.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("비공개 회고는 true를 반환한다")
    void should_return_true_for_private_review() {
      // given
      Review review = privateReview();

      // when & then
      assertThat(review.isPrivate()).isTrue();
    }
  }
}
