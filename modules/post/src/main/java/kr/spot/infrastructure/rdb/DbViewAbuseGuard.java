package kr.spot.infrastructure.rdb;

import kr.spot.IdGenerator;
import kr.spot.infrastructure.jpa.PostViewHistoryRepository;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewableType;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * DB 기반 조회 어뷰징 방지 (현재 미사용 - Redis 사용 중)
 * POST 타입만 지원
 */
//@Component
@Transactional(propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class DbViewAbuseGuard implements ViewAbuseGuard {

  private static final int TTL_MINUTES = 10;
  private static final int ZERO = 0;

  private final IdGenerator idGenerator;
  private final PostViewHistoryRepository postViewHistoryRepository;

  @Override
  public boolean shouldCount(ViewableType type, long targetId, long viewerId) {
    if (type != ViewableType.POST) {
      throw new UnsupportedOperationException("DbViewAbuseGuard only supports POST type");
    }

    int updated = postViewHistoryRepository.touchIfExpired(
        viewerId, targetId, TTL_MINUTES
    );
    if (updated > ZERO) {
      return true;
    }

    int inserted = postViewHistoryRepository.insertIfAbsent(
        idGenerator.nextId(), viewerId, targetId
    );
    return inserted > ZERO;
  }
}
