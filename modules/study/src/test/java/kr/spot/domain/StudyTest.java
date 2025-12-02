package kr.spot.domain;

import static kr.spot.common.StudyFixture.DESCRIPTION;
import static kr.spot.common.StudyFixture.FEE_AMOUNT;
import static kr.spot.common.StudyFixture.HAS_FEE;
import static kr.spot.common.StudyFixture.ID;
import static kr.spot.common.StudyFixture.IMAGE_URL;
import static kr.spot.common.StudyFixture.LEADER_ID;
import static kr.spot.common.StudyFixture.MAX_MEMBERS;
import static kr.spot.common.StudyFixture.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.domain.vo.Fee;
import kr.spot.exception.GeneralException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StudyTest {

  @Test
  @DisplayName("스터디 객체를 정상적으로 생성할 수 있다")
  void should_create_study_successfully() {
    Study study = Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
        DESCRIPTION);

    assertThat(study).isNotNull();
    assertThat(study.getId()).isEqualTo(ID);
  }

  @Test
  @DisplayName("스터디 이름이 null 이거나 공백일 경우 예외가 발생한다")
  void should_throw_exception_when_name_is_null_or_empty() {
    assertThatThrownBy(
        () -> Study.of(ID, LEADER_ID, null, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
            DESCRIPTION))
        .isInstanceOf(GeneralException.class);

    assertThatThrownBy(
        () -> Study.of(ID, LEADER_ID, "", MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
            DESCRIPTION))
        .isInstanceOf(GeneralException.class);

    assertThatThrownBy(
        () -> Study.of(ID, LEADER_ID, "   ", MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
            DESCRIPTION))
        .isInstanceOf(GeneralException.class);
  }

  @Test
  @DisplayName("최대 멤버 수가 1 미만일 경우 예외가 발생한다")
  void should_throw_exception_when_max_members_is_less_than_one() {
    assertThatThrownBy(
        () -> Study.of(ID, LEADER_ID, NAME, 0, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL, DESCRIPTION))
        .isInstanceOf(GeneralException.class);

    assertThatThrownBy(
        () -> Study.of(ID, LEADER_ID, NAME, -5, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
            DESCRIPTION))
        .isInstanceOf(GeneralException.class);
  }
}
