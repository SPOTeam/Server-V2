package kr.spot.study.domain;

import static kr.spot.study.common.StudyFixture.DESCRIPTION;
import static kr.spot.study.common.StudyFixture.FEE_AMOUNT;
import static kr.spot.study.common.StudyFixture.HAS_FEE;
import static kr.spot.study.common.StudyFixture.ID;
import static kr.spot.study.common.StudyFixture.IMAGE_URL;
import static kr.spot.study.common.StudyFixture.LEADER_ID;
import static kr.spot.study.common.StudyFixture.MAX_MEMBERS;
import static kr.spot.study.common.StudyFixture.NAME;
import static kr.spot.study.common.StudyFixture.study;
import static kr.spot.study.common.StudyMemberFixture.applied;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.vo.Fee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StudyTest {

  @Nested
  @DisplayName("스터디 생성 (of)")
  class CreateStudy {

    @Test
    @DisplayName("스터디 객체를 정상적으로 생성할 수 있다")
    void should_create_study_successfully() {
      Study study = Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT),
          IMAGE_URL, DESCRIPTION);

      assertThat(study).isNotNull();
      assertThat(study.getId()).isEqualTo(ID);
      assertThat(study.getLeaderId()).isEqualTo(LEADER_ID);
      assertThat(study.getName()).isEqualTo(NAME);
      assertThat(study.getMaxMembers()).isEqualTo(MAX_MEMBERS);
      assertThat(study.getCurrentMembers()).isEqualTo(Study.CURRENT_MEMBERS);
      assertThat(study.getRecruitingStatus()).isEqualTo(RecruitingStatus.RECRUITING);
    }

    @Test
    @DisplayName("imageUrl 없이 스터디 객체를 생성할 수 있다")
    void should_create_study_without_image_url() {
      Study study = Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT),
          DESCRIPTION);

      assertThat(study).isNotNull();
      assertThat(study.getId()).isEqualTo(ID);
      assertThat(study.getImageUrl()).isNull();
    }

    @Test
    @DisplayName("스터디 이름이 null 이거나 공백일 경우 예외가 발생한다")
    void should_throw_exception_when_name_is_null_or_empty() {
      assertThatThrownBy(
          () -> Study.of(ID, LEADER_ID, null, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
              DESCRIPTION))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);

      assertThatThrownBy(
          () -> Study.of(ID, LEADER_ID, "", MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
              DESCRIPTION))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);

      assertThatThrownBy(
          () -> Study.of(ID, LEADER_ID, "   ", MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
              DESCRIPTION))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._NAME_CAN_NOT_NULL_OR_EMPTY);
    }

    @Test
    @DisplayName("최대 멤버 수가 1 미만일 경우 예외가 발생한다")
    void should_throw_exception_when_max_members_is_less_than_one() {
      assertThatThrownBy(
          () -> Study.of(ID, LEADER_ID, NAME, 0, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
              DESCRIPTION))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._MAX_MEMBERS_MUST_BE_POSITIVE);

      assertThatThrownBy(
          () -> Study.of(ID, LEADER_ID, NAME, -5, Fee.of(HAS_FEE, FEE_AMOUNT), IMAGE_URL,
              DESCRIPTION))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._MAX_MEMBERS_MUST_BE_POSITIVE);
    }
  }

  @Nested
  @DisplayName("스터디 신청 처리 (processApplication)")
  class ProcessApplication {

    @Test
    @DisplayName("스터디장이 신청을 승인하면 APPROVE 상태로 변경된다")
    void should_change_status_to_awaiting_self_approval_when_leader_approves() {
      // given
      Study study = study();
      StudyMember application = applied(1L, study.getId(), 100L, "참여하고 싶습니다");

      // when
      study.processApplication(application, LEADER_ID, Decision.APPROVE);

      // then
      assertThat(application.getStudyMemberStatus())
          .isEqualTo(StudyMemberStatus.APPROVED);
    }

    @Test
    @DisplayName("스터디장이 신청을 거절하면 REJECTED 상태로 변경된다")
    void should_change_status_to_rejected_when_leader_rejects() {
      // given
      Study study = study();
      StudyMember application = applied(1L, study.getId(), 100L, "참여하고 싶습니다");

      // when
      study.processApplication(application, LEADER_ID, Decision.REJECT);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.REJECTED);
    }

    @Test
    @DisplayName("스터디장이 아닌 사람이 신청을 처리하면 예외가 발생한다")
    void should_throw_exception_when_non_leader_processes() {
      // given
      Study study = study();
      StudyMember application = applied(1L, study.getId(), 100L, "참여하고 싶습니다");
      Long nonLeaderId = 999L;

      // when & then
      assertThatThrownBy(
          () -> study.processApplication(application, nonLeaderId, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }

    @Test
    @DisplayName("APPLIED 상태가 아닌 신청을 처리하면 예외가 발생한다")
    void should_throw_exception_when_application_is_not_applied_status() {
      // given
      Study study = study();
      StudyMember application = applied(1L, study.getId(), 100L, "참여하고 싶습니다");
      application.decide(Decision.APPROVE); // AWAITING_SELF_APPROVAL 상태로 변경

      // when & then
      assertThatThrownBy(
          () -> study.processApplication(application, LEADER_ID, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._NOT_PENDING_APPLICATION);
    }
  }

  @Nested
  @DisplayName("스터디 신청 수신 (receiveApplication)")
  class ReceiveApplication {

    @Test
    @DisplayName("스터디에 대한 신청을 생성할 수 있다")
    void should_create_application_for_study() {
      // given
      Study study = study();
      Long applicationId = 1L;
      Long applicantId = 100L;
      String message = "참여하고 싶습니다";

      // when
      StudyMember application = study.receiveApplication(applicationId, applicantId, message);

      // then
      assertThat(application.getId()).isEqualTo(applicationId);
      assertThat(application.getStudyId()).isEqualTo(study.getId());
      assertThat(application.getMemberId()).isEqualTo(applicantId);
      assertThat(application.getMessage()).isEqualTo(message);
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPLIED);
    }
  }

  @Nested
  @DisplayName("스터디장 검증 (validateIsStudyOwner)")
  class ValidateIsStudyOwner {

    @Test
    @DisplayName("스터디장이면 검증에 성공한다")
    void should_pass_when_requester_is_leader() {
      // given
      Study study = study();

      // when & then (예외가 발생하지 않으면 성공)
      study.validateIsStudyOwner(LEADER_ID);
    }

    @Test
    @DisplayName("스터디장이 아니면 예외가 발생한다")
    void should_throw_exception_when_requester_is_not_leader() {
      // given
      Study study = study();
      Long nonLeaderId = 999L;

      // when & then
      assertThatThrownBy(() -> study.validateIsStudyOwner(nonLeaderId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }
  }

  @Nested
  @DisplayName("이미지 URL 업데이트 (updateImageUrl)")
  class UpdateImageUrl {

    @Test
    @DisplayName("이미지 URL을 업데이트할 수 있다")
    void should_update_image_url() {
      // given
      Study study = Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT),
          DESCRIPTION);
      String newImageUrl = "http://example.com/new-image.png";

      // when
      study.updateImageUrl(newImageUrl);

      // then
      assertThat(study.getImageUrl()).isEqualTo(newImageUrl);
    }

    @Test
    @DisplayName("이미지 URL을 null로 업데이트할 수 있다")
    void should_update_image_url_to_null() {
      // given
      Study study = Study.of(ID, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT),
          IMAGE_URL, DESCRIPTION);

      // when
      study.updateImageUrl(null);

      // then
      assertThat(study.getImageUrl()).isNull();
    }
  }
}
