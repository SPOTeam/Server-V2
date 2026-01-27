package kr.spot.study.application.command;

import static kr.spot.study.common.StudyMemberFixture.MEMBER_ID;
import static kr.spot.study.common.StudyMemberFixture.STUDY_ID;
import static kr.spot.study.common.StudyMemberFixture.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.enums.WithdrawReason;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.WithdrawStudyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WithdrawStudyServiceTest {

  @Mock
  StudyRepository studyRepository;
  @Mock
  StudyMemberRepository studyMemberRepository;

  WithdrawStudyService withdrawStudyService;

  @BeforeEach
  void setUp() {
    withdrawStudyService = new WithdrawStudyService(studyRepository, studyMemberRepository);
  }

  @Nested
  @DisplayName("스터디 탈퇴 (withdrawStudy)")
  class WithdrawStudy {

    @Test
    @DisplayName("일반 멤버가 탈퇴하면 상태가 WITHDRAWN으로 변경되고 회원 수가 감소한다")
    void should_change_status_to_withdrawn_and_decrease_member_count() {
      // given
      long memberId = MEMBER_ID;
      StudyMember regularMember = StudyMember.apply(1L, STUDY_ID, memberId, "참여 메시지");
      regularMember.decide(Decision.APPROVE);

      Study study = Study.of(STUDY_ID, 999L, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.NO_MORE_NEEDS, null);

      when(studyMemberRepository.getByMemberIdAndStudyIdAndStudyMemberStatusNot(memberId, STUDY_ID,
          StudyMemberStatus.WITHDRAWN))
          .thenReturn(regularMember);
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      int initialMemberCount = study.getCurrentMembers();

      // when
      withdrawStudyService.withdrawStudy(STUDY_ID, memberId, request);

      // then
      assertThat(regularMember.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.WITHDRAWN);
      assertThat(study.getCurrentMembers()).isEqualTo(initialMemberCount - 1);
    }

    @Test
    @DisplayName("오너가 다음 오너를 지정하고 탈퇴하면 권한이 위임되고 회원 수가 감소한다")
    void should_transfer_ownership_and_decrease_member_count() {
      // given
      long ownerId = 100L;
      long nextOwnerId = 2L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      StudyMember nextOwner = StudyMember.apply(nextOwnerId, STUDY_ID, 200L, "다음 오너");
      nextOwner.decide(Decision.APPROVE);

      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, nextOwnerId);

      when(studyMemberRepository.getByMemberIdAndStudyIdAndStudyMemberStatusNot(ownerId, STUDY_ID,
          StudyMemberStatus.WITHDRAWN))
          .thenReturn(ownerMember);
      when(studyMemberRepository.findByMemberIdAndStudyId(nextOwnerId, STUDY_ID)).thenReturn(
          Optional.of(nextOwner));
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      int initialMemberCount = study.getCurrentMembers();

      // when
      withdrawStudyService.withdrawStudy(STUDY_ID, ownerId, request);

      // then
      assertThat(ownerMember.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.WITHDRAWN);
      assertThat(nextOwner.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.OWNER);
      assertThat(study.getCurrentMembers()).isEqualTo(initialMemberCount - 1);
    }

    @Test
    @DisplayName("오너가 다음 오너를 지정하지 않고 탈퇴하면 예외가 발생한다")
    void should_throw_exception_when_owner_withdraws_without_next_owner() {
      // given
      long ownerId = 100L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, null);

      when(studyMemberRepository.getByMemberIdAndStudyIdAndStudyMemberStatusNot(ownerId, STUDY_ID,
          StudyMemberStatus.WITHDRAWN))
          .thenReturn(ownerMember);
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      // when & then
      assertThatThrownBy(
          () -> withdrawStudyService.withdrawStudy(STUDY_ID, ownerId, request))
          .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("다음 오너가 다른 스터디 멤버이면 예외가 발생한다")
    void should_throw_exception_when_next_owner_is_from_different_study() {
      // given
      long ownerId = 100L;
      long nextOwnerId = 2L;
      long differentStudyId = 999L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      StudyMember nextOwner = StudyMember.apply(nextOwnerId, differentStudyId, 200L, "다른 스터디 멤버");
      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, nextOwnerId);

      when(studyMemberRepository.getByMemberIdAndStudyIdAndStudyMemberStatusNot(ownerId, STUDY_ID,
          StudyMemberStatus.WITHDRAWN))
          .thenReturn(ownerMember);
      when(studyMemberRepository.findByMemberIdAndStudyId(nextOwnerId, STUDY_ID)).thenReturn(
          Optional.empty());
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      // when & then
      assertThatThrownBy(
          () -> withdrawStudyService.withdrawStudy(STUDY_ID, ownerId, request))
          .isInstanceOf(GeneralException.class);
    }
  }

  @Nested
  @DisplayName("스터디 삭제 (deleteStudy)")
  class DeleteStudy {

    @Test
    @DisplayName("오너가 혼자일 때 스터디를 삭제할 수 있다")
    void should_delete_study_when_owner_is_alone() {
      // given
      long ownerId = 100L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      when(studyMemberRepository.getByMemberIdAndStudyId(ownerId, STUDY_ID))
          .thenReturn(ownerMember);
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      // when
      withdrawStudyService.deleteStudy(STUDY_ID, ownerId);

      // then
      assertThat(study.getRecruitingStatus()).isEqualTo(RecruitingStatus.COMPLETED);
      assertThat(study.getCurrentMembers()).isEqualTo(0);
    }

    @Test
    @DisplayName("오너가 아닌 멤버가 스터디 삭제를 시도하면 예외가 발생한다")
    void should_throw_exception_when_non_owner_tries_to_delete() {
      // given
      long ownerId = 100L;
      long regularMemberId = 200L;
      StudyMember regularMember = StudyMember.apply(2L, STUDY_ID, regularMemberId, "참여 메시지");
      regularMember.decide(Decision.APPROVE);

      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      when(studyMemberRepository.getByMemberIdAndStudyId(regularMemberId, STUDY_ID))
          .thenReturn(regularMember);
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      // when & then
      assertThatThrownBy(
          () -> withdrawStudyService.deleteStudy(STUDY_ID, regularMemberId))
          .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("스터디에 멤버가 2명 이상이면 삭제할 수 없다")
    void should_throw_exception_when_study_has_multiple_members() {
      // given
      long ownerId = 100L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      Study study = Study.of(STUDY_ID, ownerId, "테스트 스터디", 10, Fee.of(false, 0), "설명");

      // 멤버 추가 (currentMembers = 2)
      StudyMember applicant = StudyMember.apply(2L, STUDY_ID, 200L, "참여 메시지");
      study.processApplication(applicant, ownerId, Decision.APPROVE);

      when(studyMemberRepository.getByMemberIdAndStudyId(ownerId, STUDY_ID))
          .thenReturn(ownerMember);
      when(studyRepository.getStudyById(STUDY_ID)).thenReturn(study);

      // when & then
      assertThatThrownBy(
          () -> withdrawStudyService.deleteStudy(STUDY_ID, ownerId))
          .isInstanceOf(GeneralException.class);
    }
  }
}
