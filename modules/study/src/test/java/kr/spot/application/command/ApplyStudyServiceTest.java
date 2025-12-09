package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.spot.code.status.ErrorStatus;
import kr.spot.common.ApplyStudyRequestFixture;
import kr.spot.common.StudyFixture;
import kr.spot.common.StudyMemberFixture;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.Decision;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.fake.FakeIdGenerator;
import kr.spot.fake.FakeStudyMemberRepository;
import kr.spot.fake.FakeStudyRepository;
import kr.spot.presentation.command.dto.request.ApplyStudyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ApplyStudyServiceTest {

  private FakeIdGenerator fakeIdGenerator;
  private FakeStudyRepository fakeStudyRepository;
  private FakeStudyMemberRepository fakeStudyMemberRepository;
  private ApplyStudyService applyStudyService;

  @BeforeEach
  void setUp() {
    fakeIdGenerator = new FakeIdGenerator(100L);
    fakeStudyRepository = new FakeStudyRepository();
    fakeStudyMemberRepository = new FakeStudyMemberRepository();
    applyStudyService = new ApplyStudyService(
        fakeIdGenerator,
        fakeStudyRepository,
        fakeStudyMemberRepository
    );
  }

  @Nested
  @DisplayName("스터디 신청 테스트")
  class ApplyStudyTest {

    @Test
    @DisplayName("스터디가 존재하면 신청에 성공한다")
    void should_apply_study_successfully_when_study_exists() {
      // given
      Long studyId = StudyFixture.ID;
      Long memberId = 10L;
      ApplyStudyRequest request = ApplyStudyRequestFixture.create();
      fakeStudyRepository.save(StudyFixture.study());

      // when
      applyStudyService.applyStudy(studyId, memberId, request);

      // then
      assertThat(fakeStudyMemberRepository.count()).isEqualTo(1);
      StudyMember savedMember = fakeStudyMemberRepository.findAll().get(0);
      assertThat(savedMember.getId()).isEqualTo(100L);
      assertThat(savedMember.getStudyId()).isEqualTo(studyId);
      assertThat(savedMember.getMemberId()).isEqualTo(memberId);
      assertThat(savedMember.getMessage()).isEqualTo(ApplyStudyRequestFixture.DEFAULT_MESSAGE);
      assertThat(savedMember.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPLIED);
    }

    @Test
    @DisplayName("스터디가 존재하지 않으면 예외가 발생한다")
    void should_throw_exception_when_study_not_found() {
      // given
      Long nonExistentStudyId = 999L;
      Long memberId = 10L;
      ApplyStudyRequest request = ApplyStudyRequestFixture.create();

      // when & then
      assertThatThrownBy(() -> applyStudyService.applyStudy(nonExistentStudyId, memberId, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("스터디 신청 처리 테스트")
  class ProcessStudyApplicationTest {

    @Test
    @DisplayName("스터디장이 신청을 승인하면 상태가 AWAITING_SELF_APPROVAL로 변경된다")
    void should_approve_application_when_leader_approves() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long leaderId = StudyMemberFixture.LEADER_ID;
      Long applicantId = 10L;
      Long applicationId = 50L;

      fakeStudyMemberRepository.save(StudyMemberFixture.owner(1L, studyId, leaderId));
      StudyMember application = StudyMemberFixture.applied(applicationId, studyId, applicantId,
          "참여하고 싶습니다");
      fakeStudyMemberRepository.save(application);

      // when
      applyStudyService.processStudyApplication(studyId, applicationId, leaderId, Decision.APPROVE);

      // then
      StudyMember processed = fakeStudyMemberRepository.getById(applicationId);
      assertThat(processed.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.AWAITING_SELF_APPROVAL);
    }

    @Test
    @DisplayName("스터디장이 신청을 거절하면 상태가 REJECTED로 변경된다")
    void should_reject_application_when_leader_rejects() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long leaderId = StudyMemberFixture.LEADER_ID;
      Long applicantId = 10L;
      Long applicationId = 50L;

      fakeStudyMemberRepository.save(StudyMemberFixture.owner(1L, studyId, leaderId));
      StudyMember application = StudyMemberFixture.applied(applicationId, studyId, applicantId,
          "참여하고 싶습니다");
      fakeStudyMemberRepository.save(application);

      // when
      applyStudyService.processStudyApplication(studyId, applicationId, leaderId, Decision.REJECT);

      // then
      StudyMember processed = fakeStudyMemberRepository.getById(applicationId);
      assertThat(processed.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.REJECTED);
    }

    @Test
    @DisplayName("스터디장이 아닌 사람이 신청을 처리하면 예외가 발생한다")
    void should_throw_exception_when_non_leader_processes() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long leaderId = StudyMemberFixture.LEADER_ID;
      Long nonLeaderId = 999L;
      Long applicationId = 50L;

      fakeStudyMemberRepository.save(StudyMemberFixture.owner(1L, studyId, leaderId));

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.processStudyApplication(studyId, applicationId, nonLeaderId,
              Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }

    @Test
    @DisplayName("존재하지 않는 신청을 처리하면 예외가 발생한다")
    void should_throw_exception_when_application_not_found() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long leaderId = StudyMemberFixture.LEADER_ID;
      Long nonExistentApplicationId = 999L;

      fakeStudyMemberRepository.save(StudyMemberFixture.owner(1L, studyId, leaderId));

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.processStudyApplication(studyId, nonExistentApplicationId,
              leaderId, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_MEMBER_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("최종 참여 결정 테스트")
  class DecideFinalParticipationTest {

    @Test
    @DisplayName("본인이 참여를 승인하면 상태가 APPROVED로 변경된다")
    void should_approve_finally_when_member_accepts() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long memberId = StudyMemberFixture.MEMBER_ID;
      Long applicationId = 50L;

      StudyMember awaitingMember = StudyMemberFixture.awaitingSelfApproval(applicationId, studyId,
          memberId);
      fakeStudyMemberRepository.save(awaitingMember);

      // when
      applyStudyService.decideFinalParticipation(studyId, memberId, Decision.APPROVE);

      // then
      StudyMember result = fakeStudyMemberRepository.getById(applicationId);
      assertThat(result.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPROVED);
    }

    @Test
    @DisplayName("본인이 참여를 거절하면 상태가 SELF_REJECTED로 변경된다")
    void should_self_reject_when_member_declines() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long memberId = StudyMemberFixture.MEMBER_ID;
      Long applicationId = 50L;

      StudyMember awaitingMember = StudyMemberFixture.awaitingSelfApproval(applicationId, studyId,
          memberId);
      fakeStudyMemberRepository.save(awaitingMember);

      // when
      applyStudyService.decideFinalParticipation(studyId, memberId, Decision.REJECT);

      // then
      StudyMember result = fakeStudyMemberRepository.getById(applicationId);
      assertThat(result.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.SELF_REJECTED);
    }

    @Test
    @DisplayName("AWAITING_SELF_APPROVAL 상태가 아닌 경우 예외가 발생한다")
    void should_throw_exception_when_not_awaiting_self_approval() {
      // given
      Long studyId = StudyMemberFixture.STUDY_ID;
      Long memberId = StudyMemberFixture.MEMBER_ID;
      Long applicationId = 50L;

      StudyMember appliedMember = StudyMemberFixture.applied(applicationId, studyId, memberId,
          "참여 희망");
      fakeStudyMemberRepository.save(appliedMember);

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.decideFinalParticipation(studyId, memberId, Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_MEMBER_NOT_FOUND);
    }
  }
}
