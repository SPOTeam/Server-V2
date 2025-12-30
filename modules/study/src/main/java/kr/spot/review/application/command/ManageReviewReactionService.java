package kr.spot.review.application.command;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.review.domain.associations.ReviewReaction;
import kr.spot.review.domain.enums.Reaction;
import kr.spot.review.infrastructure.jpa.ReviewReactionRepository;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageReviewReactionService {

  private final IdGenerator idGenerator;
  private final ReviewRepository reviewRepository;
  private final ReviewReactionRepository reviewReactionRepository;
  private final StudyAccessValidator studyAccessValidator;

  public void addReaction(long studyId, long reviewId, long memberId, Reaction reaction) {
    studyAccessValidator.validateStudyMember(studyId, memberId);
    validateReviewExists(reviewId);

    if (reviewReactionRepository.existsByReviewIdAndMemberIdAndReaction(reviewId, memberId,
        reaction)) {
      throw new GeneralException(ErrorStatus._ALREADY_REACTED);
    }

    ReviewReaction reviewReaction = ReviewReaction.of(
        idGenerator.nextId(), reviewId, memberId, reaction);
    reviewReactionRepository.save(reviewReaction);
  }

  public void removeReaction(long studyId, long reviewId, long memberId, Reaction reaction) {
    studyAccessValidator.validateStudyMember(studyId, memberId);
    validateReviewExists(reviewId);

    int deleted = reviewReactionRepository.hardDelete(reviewId, memberId, reaction.name());
    if (deleted == 0) {
      throw new GeneralException(ErrorStatus._REACTION_NOT_FOUND);
    }
  }

  private void validateReviewExists(long reviewId) {
    reviewRepository.getById(reviewId);
  }
}
