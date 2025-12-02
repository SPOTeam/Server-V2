package kr.spot.infrastructure.redis;

import kr.spot.application.ports.PostViewCounter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPostViewCounter implements PostViewCounter {

  public static final String DELTA_KEY = "view:post:%d";

  private final StringRedisTemplate redis;

  private static String deltaKey(long postId) {
    return DELTA_KEY.formatted(postId);
  }

  @Override
  public long incrementAndGetDelta(long postId) {
    Long v = redis.opsForValue().increment(deltaKey(postId));
    return v == null ? 0L : v;
  }

  @Override
  public long currentDelta(long postId) {
    String v = redis.opsForValue().get(deltaKey(postId));
    return (v == null) ? 0L : Long.parseLong(v);
  }
}
