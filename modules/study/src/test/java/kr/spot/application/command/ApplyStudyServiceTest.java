package kr.spot.application.command;

import static kr.spot.common.ApplyStudyRequestFixture.create;
import static kr.spot.common.StudyFixture.study;
import static kr.spot.common.StudyMemberFixture.applied;
import static kr.spot.common.StudyMemberFixture.awaitingSelfApproval;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.common.ApplyStudyRequestFixture;
import kr.spot.common.StudyFixture;
import kr.spot.common.StudyMemberFixture;
import kr.spot.domain.Study;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.Decision;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.StudyRepository;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.presentation.command.dto.request.ApplyStudyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplyStudyService 단위 테스트")
class ApplyStudyServiceTest {

  @InjectMocks
  private ApplyStudyService applyStudyService;

  @Mock
  private IdGenerator idGenerator;

  @Mock
  private StudyRepository studyRepository;

  @Mock
  private StudyMemberRepository studyMemberRepository;

  @Nested
  @DisplayName("스터디 신청 처리 (processStudyApplication)")
  class ProcessStudyApplication {

    private final Long applicationId = 1L;
    private final Long studyId = StudyMemberFixture.STUDY_ID;
    private final Long leaderId = StudyFixture.LEADER_ID;

    @Test
    @DisplayName("스터디장이 신청을 승인하면 AWAITING_SELF_APPROVAL 상태로 변경된다")
    void should_approve_application_when_leader_approves() {
      // given
      StudyMember application = applied(applicationId, studyId, 100L, "참여하고 싶습니다");
      Study study = study();

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);
      given(studyRepository.getStudyById(studyId)).willReturn(study);

      // when
      applyStudyService.processStudyApplication(applicationId, leaderId, Decision.APPROVE);

      // then
      assertThat(application.getStudyMemberStatus())
          .isEqualTo(StudyMemberStatus.AWAITING_SELF_APPROVAL);
    }

    @Test
    @DisplayName("스터디장이 신청을 거절하면 REJECTED 상태로 변경된다")
    void should_reject_application_when_leader_rejects() {
      // given
      StudyMember application = applied(applicationId, studyId, 100L, "참여하고 싶습니다");
      Study study = study();

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);
      given(studyRepository.getStudyById(studyId)).willReturn(study);

      // when
      applyStudyService.processStudyApplication(applicationId, leaderId, Decision.REJECT);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.REJECTED);
    }

    @Test
    @DisplayName("스터디장이 아닌 사람이 처리하면 예외가 발생한다")
    void should_throw_exception_when_non_leader_processes() {
      // given
      StudyMember application = applied(applicationId, studyId, 100L, "참여하고 싶습니다");
      Study study = study();
      Long nonLeaderId = 999L;

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);
      given(studyRepository.getStudyById(studyId)).willReturn(study);

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.processStudyApplication(applicationId, nonLeaderId,
              Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);
    }

    @Test
    @DisplayName("APPLIED 상태가 아닌 신청을 처리하면 예외가 발생한다")
    void should_throw_exception_when_not_applied_status() {
      // given
      StudyMember application = awaitingSelfApproval(applicationId, studyId, 100L);
      Study study = study();

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);
      given(studyRepository.getStudyById(studyId)).willReturn(study);

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.processStudyApplication(applicationId, leaderId,
              Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._NOT_PENDING_APPLICATION);
    }
  }

  @Nested
  @DisplayName("최종 참가 결정 (decideFinalParticipation)")
  class DecideFinalParticipation {

    private final Long applicationId = 1L;
    private final Long studyId = StudyMemberFixture.STUDY_ID;
    private final Long applicantId = StudyMemberFixture.MEMBER_ID;

    @Test
    @DisplayName("신청자가 최종 승인하면 APPROVED 상태로 변경된다")
    void should_approve_when_applicant_confirms() {
      // given
      StudyMember application = awaitingSelfApproval(applicationId, studyId, applicantId);

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);

      // when
      applyStudyService.decideFinalParticipation(applicationId, applicantId, Decision.APPROVE);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPROVED);
    }

    @Test
    @DisplayName("신청자가 최종 거절하면 SELF_REJECTED 상태로 변경된다")
    void should_self_reject_when_applicant_rejects() {
      // given
      StudyMember application = awaitingSelfApproval(applicationId, studyId, applicantId);

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);

      // when
      applyStudyService.decideFinalParticipation(applicationId, applicantId, Decision.REJECT);

      // then
      assertThat(application.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.SELF_REJECTED);
    }

    @Test
    @DisplayName("신청자가 아닌 사람이 최종 결정하면 예외가 발생한다")
    void should_throw_exception_when_non_applicant_decides() {
      // given
      StudyMember application = awaitingSelfApproval(applicationId, studyId, applicantId);
      Long otherMemberId = 999L;

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.decideFinalParticipation(applicationId, otherMemberId,
              Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_APPLICANT_CAN_SELF_APPROVE);
    }

    @Test
    @DisplayName("AWAITING_SELF_APPROVAL 상태가 아니면 예외가 발생한다")
    void should_throw_exception_when_not_awaiting_self_approval() {
      // given
      StudyMember application = applied(applicationId, studyId, applicantId, "참여하고 싶습니다");

      given(studyMemberRepository.getStudyMemberById(applicationId)).willReturn(application);

      // when & then
      assertThatThrownBy(
          () -> applyStudyService.decideFinalParticipation(applicationId, applicantId,
              Decision.APPROVE))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status",
              ErrorStatus._INVALID_STUDY_MEMBER_STATUS_FOR_SELF_APPROVAL);
    }
  }

  @Nested
  @DisplayName("스터디 신청 (applyStudy)")
  class ApplyStudy {

    private final Long studyId = StudyFixture.ID;
    private final Long memberId = 100L;
    private final Long generatedId = 999L;

    @Test
    @DisplayName("신청 이력이 없으면 신청이 정상적으로 생성된다")
    void should_create_application_when_not_already_applied() {
      // given
      Study study = study();
      ApplyStudyRequest request = create();

      given(studyRepository.getStudyById(studyId)).willReturn(study);
      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
          eq(studyId), eq(memberId), any())).willReturn(false);
      given(idGenerator.nextId()).willReturn(generatedId);

      // when
      applyStudyService.applyStudy(studyId, memberId, request);

      // then
      ArgumentCaptor<StudyMember> captor = ArgumentCaptor.forClass(StudyMember.class);
      verify(studyMemberRepository).save(captor.capture());

      StudyMember savedApplication = captor.getValue();
      assertThat(savedApplication.getId()).isEqualTo(generatedId);
      assertThat(savedApplication.getStudyId()).isEqualTo(studyId);
      assertThat(savedApplication.getMemberId()).isEqualTo(memberId);
      assertThat(savedApplication.getMessage()).isEqualTo(ApplyStudyRequestFixture.DEFAULT_MESSAGE);
      assertThat(savedApplication.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.APPLIED);
    }

    @Test
    @DisplayName("이미 신청한 스터디에 다시 신청하면 예외가 발생한다")
    void should_throw_exception_when_already_applied() {
      // given
      Study study = study();
      ApplyStudyRequest request = create();

      given(studyRepository.getStudyById(studyId)).willReturn(study);
      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
          eq(studyId), eq(memberId), any())).willReturn(true);

      // when & then
      assertThatThrownBy(() -> applyStudyService.applyStudy(studyId, memberId, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ALREADY_APPLIED);

      verify(studyMemberRepository, never()).save(any());
    }

    @Test
    @DisplayName("APPLIED, AWAITING_SELF_APPROVAL, APPROVED 상태가 있으면 중복 신청으로 간주한다")
    void should_check_active_application_statuses() {
      // given
      Study study = study();
      ApplyStudyRequest request = create();
      List<StudyMemberStatus> activeStatuses = List.of(
          StudyMemberStatus.APPLIED,
          StudyMemberStatus.AWAITING_SELF_APPROVAL,
          StudyMemberStatus.APPROVED
      );

      given(studyRepository.getStudyById(studyId)).willReturn(study);
      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatusIn(
          studyId, memberId, activeStatuses)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> applyStudyService.applyStudy(studyId, memberId, request))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ALREADY_APPLIED);
    }
  }
}
