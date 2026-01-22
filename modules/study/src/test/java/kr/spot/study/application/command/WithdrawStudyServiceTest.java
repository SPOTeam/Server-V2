package kr.spot.study.application.command;

import static kr.spot.study.common.StudyMemberFixture.MEMBER_ID;
import static kr.spot.study.common.StudyMemberFixture.STUDY_ID;
import static kr.spot.study.common.StudyMemberFixture.owner;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.Decision;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.domain.enums.WithdrawReason;
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
  StudyMemberRepository studyMemberRepository;

  WithdrawStudyService withdrawStudyService;

  @BeforeEach
  void setUp() {
    withdrawStudyService = new WithdrawStudyService(studyMemberRepository);
  }

  @Nested
  @DisplayName("스터디 탈퇴 (withdrawStudy)")
  class WithdrawStudy {

    @Test
    @DisplayName("일반 멤버가 탈퇴하면 상태가 WITHDRAWN으로 변경된다")
    void should_change_status_to_withdrawn_when_regular_member_withdraws() {
      // given
      long memberId = MEMBER_ID;
      StudyMember regularMember = StudyMember.apply(1L, STUDY_ID, memberId, "참여 메시지");
      regularMember.decide(Decision.APPROVE);

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.NO_MORE_NEEDS, null);

      when(studyMemberRepository.getByMemberIdAndStudyId(memberId, STUDY_ID))
          .thenReturn(regularMember);

      // when
      withdrawStudyService.withdrawStudy(STUDY_ID, memberId, request);

      // then
      assertThat(regularMember.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.WITHDRAWN);
    }

    @Test
    @DisplayName("오너가 다음 오너를 지정하고 탈퇴하면 권한이 위임된다")
    void should_transfer_ownership_when_owner_withdraws_with_next_owner() {
      // given
      long ownerId = 100L;
      long nextOwnerId = 2L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);
      StudyMember nextOwner = StudyMember.apply(nextOwnerId, STUDY_ID, 200L, "다음 오너");
      nextOwner.decide(Decision.APPROVE);

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, nextOwnerId);

      when(studyMemberRepository.getByMemberIdAndStudyId(ownerId, STUDY_ID))
          .thenReturn(ownerMember);
      when(studyMemberRepository.findByMemberIdAndStudyId(nextOwnerId, STUDY_ID)).thenReturn(
          Optional.of(nextOwner));

      // when
      withdrawStudyService.withdrawStudy(STUDY_ID, ownerId, request);

      // then
      assertThat(ownerMember.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.WITHDRAWN);
      assertThat(nextOwner.getStudyMemberStatus()).isEqualTo(StudyMemberStatus.OWNER);
    }

    @Test
    @DisplayName("오너가 다음 오너를 지정하지 않고 탈퇴하면 예외가 발생한다")
    void should_throw_exception_when_owner_withdraws_without_next_owner() {
      // given
      long ownerId = 100L;
      StudyMember ownerMember = owner(1L, STUDY_ID, ownerId);

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, null);

      when(studyMemberRepository.getByMemberIdAndStudyId(ownerId, STUDY_ID))
          .thenReturn(ownerMember);

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

      WithdrawStudyRequest request = new WithdrawStudyRequest(WithdrawReason.FINISHED, nextOwnerId);

      when(studyMemberRepository.getByMemberIdAndStudyId(ownerId, STUDY_ID))
          .thenReturn(ownerMember);
      when(studyMemberRepository.findByMemberIdAndStudyId(nextOwnerId, STUDY_ID)).thenReturn(
          Optional.empty());

      // when & then
      assertThatThrownBy(
          () -> withdrawStudyService.withdrawStudy(STUDY_ID, ownerId, request))
          .isInstanceOf(GeneralException.class);
    }
  }
}
