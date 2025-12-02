package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.infrastructure.jpa.PostLikeRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikePostService {

  private final IdGenerator idGenerator;
  private final PostLikeRepository postLikeRepository;
  private final PostStatsRepository postStatsRepository;

  public void likePost(Long postId, Long memberId) {
    int inserted = postLikeRepository.savePostLike(idGenerator.nextId(), postId, memberId);
    increaseLikeCount(postId, inserted);
  }

  private void increaseLikeCount(Long postId, int inserted) {
    if (inserted == 1) {
      postStatsRepository.increaseLike(postId);
    }
  }

  public void unlikePost(Long postId, Long memberId) {
    long deleted = postLikeRepository.hardDelete(postId, memberId);
    if (deleted > 0) {
      postStatsRepository.decreaseLike(postId);
    }
  }
}
