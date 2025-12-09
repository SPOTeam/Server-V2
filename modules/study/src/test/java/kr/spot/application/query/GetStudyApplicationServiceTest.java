package kr.spot.application.query;

import static kr.spot.common.MemberInfoResponseFixture.createMap;
import static kr.spot.common.StudyApplicationInfoFixture.createList;
import static kr.spot.common.StudyMemberFixture.applied;
import static kr.spot.common.StudyMemberFixture.awaitingSelfApproval;
import static kr.spot.common.StudyMemberFixture.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.common.MemberInfoResponseFixture;
import kr.spot.common.StudyApplicationInfoFixture;
import kr.spot.common.StudyMemberFixture;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.presentation.query.dto.response.GetAppliesResponse;
import kr.spot.presentation.query.dto.response.GetMyAppliedStudyResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudyApplicationService 단위 테스트")
class GetStudyApplicationServiceTest {

  @InjectMocks
  private GetStudyApplicationService getStudyApplicationService;

  @Mock
  private GetMemberInfoPort getMemberInfoPort;

  @Mock
  private StudyMemberRepository studyMemberRepository;

  @Mock
  private StudyMemberRepositoryCustom studyMemberRepositoryCustom;

  @Nested
  @DisplayName("내가 신청한 스터디 목록 조회 (getMyAppliedStudy)")
  class GetMyAppliedStudy {

    private final Long memberId = StudyMemberFixture.MEMBER_ID;

    @Test
    @DisplayName("신청한 스터디가 있으면 스터디 목록을 반환한다")
    void should_return_applied_studies_when_exists() {
      // given
      List<StudyApplicationInfo> studyApplicationInfos = createList(3);
      given(studyMemberRepositoryCustom.findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL))
          .willReturn(studyApplicationInfos);

      // when
      GetMyAppliedStudyResponse response = getStudyApplicationService.getMyAppliedStudy(memberId);

      // then
      verify(studyMemberRepositoryCustom).findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
      assertThat(response.studies()).hasSize(3);
      assertThat(response.studies().get(0).applicationId())
          .isEqualTo(studyApplicationInfos.get(0).getStudyMemberId());
      assertThat(response.studies().get(0).studyId())
          .isEqualTo(studyApplicationInfos.get(0).getStudyId());
      assertThat(response.studies().get(0).title())
          .isEqualTo(studyApplicationInfos.get(0).getStudyName());
      assertThat(response.studies().get(0).profileImageUrl())
          .isEqualTo(studyApplicationInfos.get(0).getStudyProfileImageUrl());
    }

    @Test
    @DisplayName("신청한 스터디가 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_no_applied_studies() {
      // given
      given(studyMemberRepositoryCustom.findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL))
          .willReturn(Collections.emptyList());

      // when
      GetMyAppliedStudyResponse response = getStudyApplicationService.getMyAppliedStudy(memberId);

      // then
      verify(studyMemberRepositoryCustom).findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
      assertThat(response.studies()).isEmpty();
    }
  }

  @Nested
  @DisplayName("스터디 신청 내역 조회 (getStudyApplications)")
  class GetStudyApplications {

    private final Long studyId = StudyMemberFixture.STUDY_ID;
    private final Long leaderId = StudyMemberFixture.LEADER_ID;

    @Test
    @DisplayName("스터디장이 신청 내역을 조회하면 신청자 목록을 반환한다")
    void should_return_applications_when_leader_requests() {
      // given
      StudyMember applicant1 = awaitingSelfApproval(1L, studyId, 10L);
      StudyMember applicant2 = awaitingSelfApproval(2L, studyId, 20L);
      List<StudyMember> applications = List.of(applicant1, applicant2);
      List<Long> memberIds = List.of(10L, 20L);
      Map<Long, MemberInfoResponse> memberInfos = createMap(memberIds);

      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, leaderId, StudyMemberStatus.OWNER))
          .willReturn(true);
      given(studyMemberRepositoryCustom.findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.AWAITING_SELF_APPROVAL))
          .willReturn(applications);
      given(getMemberInfoPort.getMemberInfo(memberIds))
          .willReturn(memberInfos);

      // when
      GetAppliesResponse response = getStudyApplicationService.getStudyApplications(studyId,
          leaderId);

      // then
      verify(studyMemberRepository).existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, leaderId, StudyMemberStatus.OWNER);
      verify(studyMemberRepositoryCustom).findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
      verify(getMemberInfoPort).getMemberInfo(memberIds);

      assertThat(response.applies()).hasSize(2);
      assertThat(response.applies().get(0).applicantId()).isEqualTo(applicant1.getId());
      assertThat(response.applies().get(0).memberId()).isEqualTo(applicant1.getMemberId());
      assertThat(response.applies().get(0).nickname())
          .isEqualTo(memberInfos.get(applicant1.getMemberId()).name());
      assertThat(response.applies().get(0).description()).isEqualTo(applicant1.getMessage());
      assertThat(response.applies().get(0).profileImageUrl())
          .isEqualTo(memberInfos.get(applicant1.getMemberId()).profileImageUrl());
    }

    @Test
    @DisplayName("신청자가 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_no_applications() {
      // given
      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, leaderId, StudyMemberStatus.OWNER))
          .willReturn(true);
      given(studyMemberRepositoryCustom.findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.AWAITING_SELF_APPROVAL))
          .willReturn(Collections.emptyList());
      given(getMemberInfoPort.getMemberInfo(Collections.emptyList()))
          .willReturn(Collections.emptyMap());

      // when
      GetAppliesResponse response = getStudyApplicationService.getStudyApplications(studyId,
          leaderId);

      // then
      verify(studyMemberRepository).existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, leaderId, StudyMemberStatus.OWNER);
      verify(studyMemberRepositoryCustom).findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.AWAITING_SELF_APPROVAL);
      assertThat(response.applies()).isEmpty();
    }

    @Test
    @DisplayName("스터디장이 아닌 사람이 조회하면 예외가 발생한다")
    void should_throw_exception_when_not_leader() {
      // given
      Long nonLeaderId = 999L;
      given(studyMemberRepository.existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, nonLeaderId, StudyMemberStatus.OWNER))
          .willReturn(false);

      // when & then
      assertThatThrownBy(
          () -> getStudyApplicationService.getStudyApplications(studyId, nonLeaderId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);

      verify(studyMemberRepository).existsByStudyIdAndMemberIdAndStudyMemberStatus(
          studyId, nonLeaderId, StudyMemberStatus.OWNER);
      verify(studyMemberRepositoryCustom, never()).findApplicationsByStudyIdAndStatus(any(), any());
      verify(getMemberInfoPort, never()).getMemberInfo(any());
    }
  }
}