package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.domain.PostStatsByOptimisticLock;
import kr.spot.domain.PostStatsByPessimisticLock;
import kr.spot.infrastructure.jpa.PostLikeRepository;
import kr.spot.infrastructure.jpa.PostStatsByOptimisticLockRepository;
import kr.spot.infrastructure.jpa.PostStatsByPessimisticLockRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LikePostTestService {

  private final IdGenerator idGenerator;
  private final PostLikeRepository postLikeRepository;
  private final PostStatsRepository postStatsRepository;

  private final PostStatsByOptimisticLockRepository postStatsByOptimisticLockRepository;
  private final PostStatsByPessimisticLockRepository postStatsByPessimisticLockRepository;

  /**
   * 1) 기본 방식 (atomic increment)
   */
  public void likePost(Long postId, Long memberId) {
    int inserted = postLikeRepository.savePostLike(idGenerator.nextId(), postId, memberId);
    increaseLikeCount(postId, inserted);
  }

  /**
   * 2) 낙관적 락 방식
   */
  public void likePostOptimistic(Long postId, Long memberId) {
    int inserted = postLikeRepository.savePostLike(idGenerator.nextId(), postId, memberId);
    if (inserted != 1) {
      return;
    }

    final int MAX_RETRY = 5;
    for (int i = 0; i < MAX_RETRY; i++) {
      try {
        PostStatsByOptimisticLock stats =
            postStatsByOptimisticLockRepository.findById(postId)
                .orElseGet(() -> PostStatsByOptimisticLock.of(postId, 0L));
        stats.incrementLikeCount();
        postStatsByOptimisticLockRepository.saveAndFlush(stats);
        return;
      } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
        if (i == MAX_RETRY - 1) {
          throw e;
        }
      }
    }
  }

  /**
   * 3) 비관적 락 방식
   */
  public void likePostPessimistic(Long postId, Long memberId) {
    int inserted = postLikeRepository.savePostLike(idGenerator.nextId(), postId, memberId);
    if (inserted != 1) {
      return;
    }
    PostStatsByPessimisticLock stats =
        postStatsByPessimisticLockRepository.findByPostId(postId)
            .orElseGet(() -> PostStatsByPessimisticLock.of(postId, 0L));
    stats.incrementLikeCount();
    postStatsByPessimisticLockRepository.saveAndFlush(stats);
  }

  private void increaseLikeCount(Long postId, int inserted) {
    if (inserted == 1) {
      postStatsRepository.increaseLike(postId);
    }
  }
}
