package kr.spot.post.application.command;

import kr.spot.IdGenerator;
import kr.spot.post.infrastructure.jpa.PostLikeRepository;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "studyLikePostService")
@Transactional
@RequiredArgsConstructor
public class LikePostService {

  private final IdGenerator idGenerator;
  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final PostStatsRepository postStatsRepository;
  private final StudyAccessValidator accessValidator;

  public void likePost(long studyId, long postId, long memberId) {
    validateAccess(studyId, postId, memberId);

    int inserted = postLikeRepository.savePostLike(idGenerator.nextId(), postId, memberId);
    if (inserted == 1) {
      postStatsRepository.increaseLike(postId);
    }
  }

  public void unlikePost(long studyId, long postId, long memberId) {
    validateAccess(studyId, postId, memberId);

    int deleted = postLikeRepository.hardDelete(postId, memberId);
    if (deleted > 0) {
      postStatsRepository.decreaseLike(postId);
    }
  }

  private void validateAccess(long studyId, long postId, long memberId) {
    accessValidator.validateStudyMember(studyId, memberId);
    postRepository.getById(postId).validateBelongsToStudy(studyId);
  }
}
