package kr.spot.application.query;

import static kr.spot.common.MemberInfoResponseFixture.createMap;
import static kr.spot.common.StudyApplicationInfoFixture.createList;
import static kr.spot.common.StudyMemberFixture.applied;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.common.StudyMemberFixture;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetMemberInfoPort;
import kr.spot.ports.dto.MemberInfoResponse;
import kr.spot.study.application.query.GetStudyApplicationService;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyMemberRepositoryCustom;
import kr.spot.study.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;
import kr.spot.study.presentation.query.dto.response.GetAppliesResponse;
import kr.spot.study.presentation.query.dto.response.GetMyAppliedStudyResponse;
import org.assertj.core.api.Assertions;
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
  private StudyMemberRepositoryCustom studyMemberRepositoryCustom;

  @Mock
  private StudyAccessValidator studyAccessValidator;

  @Nested
  @DisplayName("내가 신청한 스터디 목록 조회 (getMyAppliedStudy)")
  class GetMyAppliedStudy {

    private final Long memberId = StudyMemberFixture.MEMBER_ID;

    @Test
    @DisplayName("신청한 스터디가 있으면 목록을 반환한다")
    void should_return_applied_studies_when_exists() {
      // given
      List<StudyApplicationInfo> applications = createList(3);
      given(studyMemberRepositoryCustom.findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL))
          .willReturn(applications);

      // when
      GetMyAppliedStudyResponse response = getStudyApplicationService.getMyAppliedStudy(memberId);

      // then
      verify(studyMemberRepositoryCustom).findMyAppliedStudiesWithStudyInfo(
          memberId, StudyMemberStatus.AWAITING_SELF_APPROVAL);

      assertThat(response.studies()).hasSize(3);
      assertThat(response.studies().get(0).applicationId())
          .isEqualTo(applications.get(0).getStudyMemberId());
      assertThat(response.studies().get(0).studyId())
          .isEqualTo(applications.get(0).getStudyId());
      assertThat(response.studies().get(0).title())
          .isEqualTo(applications.get(0).getStudyName());
      assertThat(response.studies().get(0).profileImageUrl())
          .isEqualTo(applications.get(0).getStudyProfileImageUrl());
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
      StudyMember applicant1 = applied(1L, studyId, 10L, "참여하고 싶습니다");
      StudyMember applicant2 = applied(2L, studyId, 20L, "열심히 하겠습니다");
      List<StudyMember> applications = List.of(applicant1, applicant2);
      List<Long> memberIds = List.of(10L, 20L);
      Map<Long, MemberInfoResponse> memberInfos = createMap(memberIds);

      given(studyMemberRepositoryCustom.findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.APPLIED))
          .willReturn(applications);
      given(getMemberInfoPort.getMemberInfo(memberIds))
          .willReturn(memberInfos);

      // when
      GetAppliesResponse response = getStudyApplicationService.getStudyApplications(studyId,
          leaderId);

      // then
      verify(studyAccessValidator).validateStudyLeader(studyId, leaderId);
      verify(studyMemberRepositoryCustom).findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.APPLIED);
      verify(getMemberInfoPort).getMemberInfo(memberIds);

      assertThat(response.applies()).hasSize(2);
      assertThat(response.applies().get(0).applicantId()).isEqualTo(applicant1.getId());
      assertThat(response.applies().get(0).memberId()).isEqualTo(applicant1.getMemberId());
      assertThat(response.applies().get(0).nickname())
          .isEqualTo(memberInfos.get(applicant1.getMemberId()).name());
      assertThat(response.applies().get(0).description()).isEqualTo(applicant1.getMessage());
    }

    @Test
    @DisplayName("신청자가 없으면 빈 목록을 반환한다")
    void should_return_empty_list_when_no_applications() {
      // given
      given(studyMemberRepositoryCustom.findApplicationsByStudyIdAndStatus(
          studyId, StudyMemberStatus.APPLIED))
          .willReturn(Collections.emptyList());

      // when
      GetAppliesResponse response = getStudyApplicationService.getStudyApplications(studyId,
          leaderId);

      // then
      verify(studyAccessValidator).validateStudyLeader(studyId, leaderId);
      assertThat(response.applies()).isEmpty();
    }

    @Test
    @DisplayName("스터디장이 아닌 사람이 조회하면 예외가 발생한다")
    void should_throw_exception_when_not_leader() {
      // given
      Long nonLeaderId = 999L;
      willThrow(new GeneralException(ErrorStatus._ONLY_LEADER_CAN_ACCESS))
          .given(studyAccessValidator).validateStudyLeader(studyId, nonLeaderId);

      // when & then
      Assertions.assertThatThrownBy(
              () -> getStudyApplicationService.getStudyApplications(studyId, nonLeaderId))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_LEADER_CAN_ACCESS);

      verify(studyAccessValidator).validateStudyLeader(studyId, nonLeaderId);
    }
  }
}
