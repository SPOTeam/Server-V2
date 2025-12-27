package kr.spot.infrastructure.redis;

import java.time.Duration;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewableType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisViewAbuseGuard implements ViewAbuseGuard {

  private static final String GUARD_KEY_FORMAT = "view:guard:%s:%d:%d";
  private static final String VALUE = "1";
  private static final Duration WINDOW = Duration.ofMinutes(10);

  private final StringRedisTemplate redis;

  private static String guardKey(ViewableType type, long targetId, long viewerId) {
    return GUARD_KEY_FORMAT.formatted(type.getKeyPrefix(), targetId, viewerId);
  }

  @Override
  public boolean shouldCount(ViewableType type, long targetId, long viewerId) {
    String key = guardKey(type, targetId, viewerId);
    Boolean ok = redis.opsForValue().setIfAbsent(key, VALUE, WINDOW);
    return Boolean.TRUE.equals(ok);
  }
}
