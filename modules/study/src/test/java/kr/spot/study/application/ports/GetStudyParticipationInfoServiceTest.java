package kr.spot.study.application.ports;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.spot.ports.dto.StudyParticipationInfo;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("GetStudyParticipationInfoService 단위 테스트")
class GetStudyParticipationInfoServiceTest {

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Mock
  StudyRepository studyRepository;

  @InjectMocks
  GetStudyParticipationInfoService sut;

  @Nested
  @DisplayName("getStudyParticipationInfo 메서드는")
  class GetStudyParticipationInfoTest {

    private final long memberId = 1L;

    @Test
    @DisplayName("회원의 스터디 참여 정보를 정상적으로 조회한다")
    void should_return_study_participation_info() {
      // given
      long expectedParticipatingCount = 3L;
      long expectedRecruitingCount = 1L;
      long expectedAppliedCount = 2L;

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
          memberId,
          List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
      )).willReturn(expectedParticipatingCount);

      given(studyRepository.countByLeaderIdAndRecruitingStatus(
          memberId,
          RecruitingStatus.RECRUITING
      )).willReturn(expectedRecruitingCount);

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatus(
          memberId,
          StudyMemberStatus.APPLIED
      )).willReturn(expectedAppliedCount);

      // when
      StudyParticipationInfo result = sut.getStudyParticipationInfo(memberId);

      // then
      assertThat(result.participatingStudyCount()).isEqualTo(expectedParticipatingCount);
      assertThat(result.recruitingStudyCount()).isEqualTo(expectedRecruitingCount);
      assertThat(result.appliedStudyCount()).isEqualTo(expectedAppliedCount);
    }

    @Test
    @DisplayName("참여 중인 스터디가 없으면 0을 반환한다")
    void should_return_zero_when_no_participating_studies() {
      // given
      given(studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
          memberId,
          List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
      )).willReturn(0L);

      given(studyRepository.countByLeaderIdAndRecruitingStatus(
          memberId,
          RecruitingStatus.RECRUITING
      )).willReturn(0L);

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatus(
          memberId,
          StudyMemberStatus.APPLIED
      )).willReturn(0L);

      // when
      StudyParticipationInfo result = sut.getStudyParticipationInfo(memberId);

      // then
      assertThat(result.participatingStudyCount()).isZero();
      assertThat(result.recruitingStudyCount()).isZero();
      assertThat(result.appliedStudyCount()).isZero();
    }

    @Test
    @DisplayName("OWNER와 APPROVED 상태의 스터디만 참여 중으로 카운트한다")
    void should_count_only_owner_and_approved_as_participating() {
      // given
      long participatingCount = 5L;

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
          memberId,
          List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
      )).willReturn(participatingCount);

      given(studyRepository.countByLeaderIdAndRecruitingStatus(
          memberId,
          RecruitingStatus.RECRUITING
      )).willReturn(0L);

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatus(
          memberId,
          StudyMemberStatus.APPLIED
      )).willReturn(0L);

      // when
      StudyParticipationInfo result = sut.getStudyParticipationInfo(memberId);

      // then
      assertThat(result.participatingStudyCount()).isEqualTo(participatingCount);
    }

    @Test
    @DisplayName("RECRUITING 상태의 스터디만 모집 중으로 카운트한다")
    void should_count_only_recruiting_status_as_recruiting() {
      // given
      long recruitingCount = 2L;

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
          memberId,
          List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
      )).willReturn(0L);

      given(studyRepository.countByLeaderIdAndRecruitingStatus(
          memberId,
          RecruitingStatus.RECRUITING
      )).willReturn(recruitingCount);

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatus(
          memberId,
          StudyMemberStatus.APPLIED
      )).willReturn(0L);

      // when
      StudyParticipationInfo result = sut.getStudyParticipationInfo(memberId);

      // then
      assertThat(result.recruitingStudyCount()).isEqualTo(recruitingCount);
    }

    @Test
    @DisplayName("APPLIED 상태의 스터디만 신청 중으로 카운트한다")
    void should_count_only_applied_status_as_applied() {
      // given
      long appliedCount = 4L;

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
          memberId,
          List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
      )).willReturn(0L);

      given(studyRepository.countByLeaderIdAndRecruitingStatus(
          memberId,
          RecruitingStatus.RECRUITING
      )).willReturn(0L);

      given(studyMemberRepository.countByMemberIdAndStudyMemberStatus(
          memberId,
          StudyMemberStatus.APPLIED
      )).willReturn(appliedCount);

      // when
      StudyParticipationInfo result = sut.getStudyParticipationInfo(memberId);

      // then
      assertThat(result.appliedStudyCount()).isEqualTo(appliedCount);
    }
  }
}
