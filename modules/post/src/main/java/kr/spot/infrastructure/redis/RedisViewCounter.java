package kr.spot.infrastructure.redis;

import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisViewCounter implements ViewCounter {

  private static final String DELTA_KEY_FORMAT = "view:%s:%d";

  private final StringRedisTemplate redis;

  private static String deltaKey(ViewableType type, long targetId) {
    return DELTA_KEY_FORMAT.formatted(type.getKeyPrefix(), targetId);
  }

  @Override
  public long incrementAndGet(ViewableType type, long targetId) {
    Long v = redis.opsForValue().increment(deltaKey(type, targetId));
    return v == null ? 0L : v;
  }

  @Override
  public long currentDelta(ViewableType type, long targetId) {
    String v = redis.opsForValue().get(deltaKey(type, targetId));
    return (v == null) ? 0L : Long.parseLong(v);
  }
}
