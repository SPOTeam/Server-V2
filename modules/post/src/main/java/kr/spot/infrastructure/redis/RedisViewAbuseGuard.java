package kr.spot.infrastructure.redis;

import java.time.Duration;
import kr.spot.application.ports.ViewAbuseGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisViewAbuseGuard implements ViewAbuseGuard {

  public static final String GUARD_KEY = "view:guard:%d:%d";
  public static final String VALUE = "1";
  private static final Duration WINDOW = Duration.ofMinutes(10);
  
  private final StringRedisTemplate redis;

  private static String guardKey(long postId, long viewerId) {
    return GUARD_KEY.formatted(postId, viewerId);
  }

  @Override
  public boolean shouldCount(long postId, long viewerId) {
    String key = guardKey(postId, viewerId);
    Boolean ok = redis.opsForValue().setIfAbsent(key, VALUE, WINDOW);
    return Boolean.TRUE.equals(ok);
  }
}
