package kr.spot.review.application.command;

import static kr.spot.review.common.ReviewFixture.MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.STUDY_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.review.domain.associations.ReviewReaction;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.infrastructure.jpa.ReviewReactionRepository;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ManageReviewReactionServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  ReviewRepository reviewRepository;

  @Mock
  ReviewReactionRepository reviewReactionRepository;

  @Mock
  StudyAccessValidator studyAccessValidator;

  @Captor
  ArgumentCaptor<ReviewReaction> reactionCaptor;

  ManageReviewReactionService manageReviewReactionService;

  @BeforeEach
  void setUp() {
    manageReviewReactionService = new ManageReviewReactionService(
        idGenerator, reviewRepository, reviewReactionRepository, studyAccessValidator);
  }

  @Nested
  @DisplayName("반응 추가 (addReaction)")
  class AddReaction {

    @Test
    @DisplayName("반응을 정상적으로 추가할 수 있다")
    void should_add_reaction_successfully() {
      // given
      long reviewId = 1L;
      long generatedId = 100L;
      Reaction reaction = Reaction.FIRE;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doNothing().when(reviewRepository).validateExists(anyLong());
      when(reviewReactionRepository.existsByReviewIdAndMemberIdAndReaction(anyLong(), anyLong(),
          any())).thenReturn(false);
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(reviewReactionRepository.save(any(ReviewReaction.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageReviewReactionService.addReaction(STUDY_ID, reviewId, MEMBER_ID, reaction);

      // then
      verify(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(reviewRepository).validateExists(reviewId);
      verify(reviewReactionRepository).save(reactionCaptor.capture());

      ReviewReaction capturedReaction = reactionCaptor.getValue();
      assertThat(capturedReaction.getId()).isEqualTo(generatedId);
      assertThat(capturedReaction.getReviewId()).isEqualTo(reviewId);
      assertThat(capturedReaction.getMemberId()).isEqualTo(MEMBER_ID);
      assertThat(capturedReaction.getReaction()).isEqualTo(reaction);
    }

    @Test
    @DisplayName("이미 동일한 반응이 있으면 무시한다 (멱등성)")
    void should_ignore_when_already_reacted() {
      // given
      long reviewId = 1L;
      Reaction reaction = Reaction.HEART;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doNothing().when(reviewRepository).validateExists(anyLong());
      when(reviewReactionRepository.existsByReviewIdAndMemberIdAndReaction(anyLong(), anyLong(),
          any())).thenReturn(true);

      // when
      manageReviewReactionService.addReaction(STUDY_ID, reviewId, MEMBER_ID, reaction);

      // then
      verify(reviewReactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 회고에 반응을 추가하려고 하면 예외가 발생한다")
    void should_throw_exception_when_review_not_found() {
      // given
      long reviewId = 999L;
      Reaction reaction = Reaction.STAR;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doThrow(new GeneralException(ErrorStatus._REVIEW_NOT_FOUND))
          .when(reviewRepository).validateExists(anyLong());

      // when & then
      assertThatThrownBy(
          () -> manageReviewReactionService.addReaction(STUDY_ID, reviewId, MEMBER_ID, reaction))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 반응을 추가할 수 없다")
    void should_throw_exception_when_not_study_member() {
      // given
      long reviewId = 1L;
      Reaction reaction = Reaction.SMILE;

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());

      // when & then
      assertThatThrownBy(
          () -> manageReviewReactionService.addReaction(STUDY_ID, reviewId, MEMBER_ID, reaction))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);

      verify(reviewReactionRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("반응 제거 (removeReaction)")
  class RemoveReaction {

    @Test
    @DisplayName("반응을 정상적으로 제거할 수 있다")
    void should_remove_reaction_successfully() {
      // given
      long reviewId = 1L;
      Reaction reaction = Reaction.FIRE;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doNothing().when(reviewRepository).validateExists(anyLong());
      when(reviewReactionRepository.hardDelete(anyLong(), anyLong(), anyString())).thenReturn(1);

      // when
      manageReviewReactionService.removeReaction(STUDY_ID, reviewId, MEMBER_ID, reaction);

      // then
      verify(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(reviewRepository).validateExists(reviewId);
      verify(reviewReactionRepository).hardDelete(reviewId, MEMBER_ID, reaction.name());
    }

    @Test
    @DisplayName("존재하지 않는 반응을 제거해도 예외가 발생하지 않는다 (멱등성)")
    void should_not_throw_when_reaction_not_found() {
      // given
      long reviewId = 1L;
      Reaction reaction = Reaction.HEART;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doNothing().when(reviewRepository).validateExists(anyLong());
      when(reviewReactionRepository.hardDelete(anyLong(), anyLong(), anyString())).thenReturn(0);

      // when & then (예외 발생 안함)
      manageReviewReactionService.removeReaction(STUDY_ID, reviewId, MEMBER_ID, reaction);

      verify(reviewReactionRepository).hardDelete(reviewId, MEMBER_ID, reaction.name());
    }

    @Test
    @DisplayName("존재하지 않는 회고의 반응을 제거하려고 하면 예외가 발생한다")
    void should_throw_exception_when_review_not_found() {
      // given
      long reviewId = 999L;
      Reaction reaction = Reaction.STAR;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      doThrow(new GeneralException(ErrorStatus._REVIEW_NOT_FOUND))
          .when(reviewRepository).validateExists(anyLong());

      // when & then
      assertThatThrownBy(
          () -> manageReviewReactionService.removeReaction(STUDY_ID, reviewId, MEMBER_ID, reaction))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 반응을 제거할 수 없다")
    void should_throw_exception_when_not_study_member() {
      // given
      long reviewId = 1L;
      Reaction reaction = Reaction.SMILE;

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());

      // when & then
      assertThatThrownBy(
          () -> manageReviewReactionService.removeReaction(STUDY_ID, reviewId, MEMBER_ID, reaction))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);

      verify(reviewReactionRepository, never()).hardDelete(anyLong(), anyLong(), anyString());
    }
  }
}
