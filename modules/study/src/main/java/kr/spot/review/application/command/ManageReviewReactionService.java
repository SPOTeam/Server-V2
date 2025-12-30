package kr.spot.review.application.command;

import kr.spot.IdGenerator;
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
    reviewRepository.validateExists(reviewId);

    if (reviewReactionRepository.existsByReviewIdAndMemberIdAndReaction(reviewId, memberId,
        reaction)) {
      return;
    }

    ReviewReaction reviewReaction = ReviewReaction.of(
        idGenerator.nextId(), reviewId, memberId, reaction);
    reviewReactionRepository.save(reviewReaction);
  }

  public void removeReaction(long studyId, long reviewId, long memberId, Reaction reaction) {
    studyAccessValidator.validateStudyMember(studyId, memberId);
    reviewRepository.validateExists(reviewId);

    reviewReactionRepository.hardDelete(reviewId, memberId, reaction.name());
  }
}
