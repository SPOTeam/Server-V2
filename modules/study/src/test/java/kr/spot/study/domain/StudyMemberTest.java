package kr.spot.study.domain;

import static kr.spot.study.common.StudyMemberFixture.ID;
import static kr.spot.study.common.StudyMemberFixture.MEMBER_ID;
import static kr.spot.study.common.StudyMemberFixture.MESSAGE;
import static kr.spot.study.common.StudyMemberFixture.STUDY_ID;
import static kr.spot.study.common.StudyMemberFixture.applied;
import static kr.spot.study.common.StudyMemberFixture.awaitingSelfApproval;
import static kr.spot.study.common.StudyMemberFixture.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.StudyMemberStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("StudyMember 도메인 테스트")
class StudyMemberTest {

  @Nested
  @DisplayName("스터디 멤버 생성 (create)")
  class Create {

    @Test
    @DisplayName("스터디장으로 스터디 멤버를 생성할 수 있다")
    void should_create_owner_successfully() {
      // when
      StudyMember owner = StudyMember.create(ID, STUDY_ID, MEMBER_ID);

      // then
      assertThat(owner.getId()).isEqualTo(ID);
      assertThat(owner.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(owner.getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(owner.getMessage()).isNull();
      assertThat(owner.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.OWNER);
    }
  }

  @Nested
  @DisplayName("스터디 신청 (apply)")
  class Apply {

    @Test
    @DisplayName("스터디에 신청하면 APPLIED 상태로 생성된다")
    void should_create_applied_member_successfully() {
      // when
      StudyMember application = StudyMember.apply(ID, STUDY_ID, MEMBER_ID, MESSAGE);

      // then
      assertThat(application.getId()).isEqualTo(ID);
      assertThat(application.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(application.getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(application.getMessage()).isEqualTo(MESSAGE);
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPLIED);
    }
  }

  @Nested
  @DisplayName("신청 결정 (decide)")
  class Decide {

    @Test
    @DisplayName("승인하면 AWAITING_SELF_APPROVAL 상태로 변경된다")
    void should_change_to_awaiting_self_approval_when_approved() {
      // given
      StudyMember application = applied();

      // when
      application.decide(Decision.APPROVE);

      // then
      assertThat(application.getStudyMemberStatus())
          .isEqualTo(StudyMemberStatus.AWAITING_SELF_APPROVAL);
    }

    @Test
    @DisplayName("거절하면 REJECTED 상태로 변경된다")
    void should_change_to_rejected_when_rejected() {
      // given
      StudyMember application = applied();

      // when
      application.decide(Decision.REJECT);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.REJECTED);
    }
  }

  @Nested
  @DisplayName("신청자의 최종 결정 (decideFinalByApplicant)")
  class DecideFinalByApplicant {

    @Test
    @DisplayName("신청자 본인이 최종 승인하면 APPROVED 상태로 변경된다")
    void should_change_to_approved_when_applicant_self_approves() {
      // given
      StudyMember application = awaitingSelfApproval(ID, STUDY_ID, MEMBER_ID);

      // when
      application.decideFinalByApplicant(MEMBER_ID, Decision.APPROVE);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPROVED);
    }

    @Test
    @DisplayName("신청자 본인이 최종 거절하면 SELF_REJECTED 상태로 변경된다")
    void should_change_to_self_rejected_when_applicant_self_rejects() {
      // given
      StudyMember application = awaitingSelfApproval(ID, STUDY_ID, MEMBER_ID);

      // when
      application.decideFinalByApplicant(MEMBER_ID, Decision.REJECT);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.SELF_REJECTED);
    }

    @Test
    @DisplayName("신청자 본인이 아닌 사람이 최종 결정하면 예외가 발생한다")
    void should_throw_exception_when_non_applicant_decides() {
      // given
      StudyMember application = awaitingSelfApproval(ID, STUDY_ID, MEMBER_ID);
      Long otherMemberId = 999L;

      // when & then
      assertThatThrownBy(() -> application.decideFinalByApplicant(otherMemberId, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_APPLICANT_CAN_SELF_APPROVE);
    }

    @Test
    @DisplayName("AWAITING_SELF_APPROVAL 상태가 아닌 신청에 최종 결정하면 예외가 발생한다")
    void should_throw_exception_when_status_is_not_awaiting_self_approval() {
      // given
      StudyMember application = applied(); // APPLIED 상태

      // when & then
      assertThatThrownBy(() -> application.decideFinalByApplicant(MEMBER_ID, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status",
              ErrorStatus._INVALID_STUDY_MEMBER_STATUS_FOR_SELF_APPROVAL);
    }

    @Test
    @DisplayName("OWNER 상태의 멤버는 최종 결정할 수 없다")
    void should_throw_exception_when_owner_tries_to_decide() {
      // given
      StudyMember ownerMember = owner(ID, STUDY_ID, MEMBER_ID);

      // when & then
      assertThatThrownBy(() -> ownerMember.decideFinalByApplicant(MEMBER_ID, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status",
              ErrorStatus._INVALID_STUDY_MEMBER_STATUS_FOR_SELF_APPROVAL);
    }
  }
}
