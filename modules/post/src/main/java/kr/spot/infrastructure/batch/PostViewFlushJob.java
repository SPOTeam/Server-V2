package kr.spot.infrastructure.batch;

import kr.spot.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewFlushJob {

  public static final String KEY_PREFIX = "view:post:*";

  private final StringRedisTemplate redis;
  private final PostStatsRepository postStatsRepository;

  @Transactional
  @Scheduled(cron = "0 * * * * *")
  public void flush() {
    log.info("PostViewFlushJob started");
    ScanOptions options = ScanOptions.scanOptions()
        .match(KEY_PREFIX)
        .count(500)
        .build();

    // 같은 커넥션 안에서 SCAN → GET → DEL까지 처리
    redis.execute((RedisCallback<Object>) (connection) -> {
      try (Cursor<byte[]> cursor = connection.scan(options)) {
        while (cursor.hasNext()) {
          byte[] k = cursor.next();

          // 값 읽기 (동일 커넥션)
          byte[] v = connection.stringCommands().get(k);
          long delta = parseLongSafe(v);
          if (delta <= 0) {
            continue;
          }

          Long postId = parsePostIdFromKey(k);

          // DB에 원자적 합산
          postStatsRepository.increaseViewBy(postId, delta);

          // 처리 완료 후 키 삭제
          connection.keyCommands().del(k);
        }
      } catch (Exception e) {
        // 실패 시 키를 남겨두면 다음 주기에 재시도 가능
        // 필요 시 로그 추가
      }
      return null;
    });
  }

  private long parseLongSafe(byte[] v) {
    if (v == null || v.length == 0) {
      return 0L;
    }
    try {
      return Long.parseLong(new String(v, java.nio.charset.StandardCharsets.UTF_8));
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  private Long parsePostIdFromKey(byte[] keyBytes) {
    String key = new String(keyBytes, java.nio.charset.StandardCharsets.UTF_8);
    // "view:post:{postId}"
    return Long.valueOf(key.substring("view:post:".length()));
  }
}
