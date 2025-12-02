package kr.spot.infrastructure.rdb;

import kr.spot.IdGenerator;
import kr.spot.application.ports.ViewAbuseGuard;
import kr.spot.infrastructure.jpa.PostViewHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

//@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class DbViewAbuseGuard implements ViewAbuseGuard {

  private final IdGenerator idGenerator;
  private final PostViewHistoryRepository postViewHistoryRepository;

  @Override
  public boolean shouldCount(long postId, long viewerId) {
    int inserted = postViewHistoryRepository.insertIgnore(
        idGenerator.nextId(), viewerId, postId);
    return inserted > 0;
  }
}
