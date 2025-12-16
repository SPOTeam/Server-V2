package kr.spot.infrastructure.batch;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostViewFlushJob {

  private static final String KEY_PREFIX = "view:post:";
  private static final String KEY_PATTERN = KEY_PREFIX + "*";
  private static final String LOCK_KEY = "lock:view-flush";

  private final StringRedisTemplate redis;
  private final PostStatsRepository postStatsRepository;

  @Scheduled(cron = "0 * * * * *")
  public void flush() {
    if (!tryAcquireLock()) {
      log.debug("다른 인스턴스에서 실행 중");
      return;
    }
    try {
      executeBatch();
    } finally {
      releaseLock();
    }
  }

  private void executeBatch() {
    log.info("PostViewFlushJob 시작");
    long startTime = System.currentTimeMillis();

    BatchResult result = processAllKeys();

    long duration = System.currentTimeMillis() - startTime;
    log.info("PostViewFlushJob 완료: 성공 {}건, 실패 {}건, {}ms",
        result.successCount, result.failureCount, duration);
  }

  private BatchResult processAllKeys() {
    BatchResult result = new BatchResult();
    ScanOptions options = ScanOptions.scanOptions()
        .match(KEY_PATTERN)
        .count(500)
        .build();

    redis.execute((RedisCallback<Void>) connection -> {
      try (Cursor<byte[]> cursor = connection.scan(options)) {
        while (cursor.hasNext()) {
          byte[] keyBytes = cursor.next();
          processKeyWithTransaction(keyBytes, result);
        }
      } catch (Exception e) {
        log.error("SCAN 처리 중 오류", e);
      }
      return null;
    });

    return result;
  }

  private void processKeyWithTransaction(byte[] keyBytes, BatchResult result) {
    String key = new String(keyBytes, StandardCharsets.UTF_8);

    try {
      // 1. Redis 값 읽기 (트랜잭션 밖)
      String valueStr = redis.opsForValue().get(key);
      if (valueStr == null) {
        return;
      }

      // 2. 값 검증 (트랜잭션 밖)
      long delta = parseLong(valueStr);
      if (delta <= 0) {
        log.warn("유효하지 않은 값, 키 삭제: key={}, value={}", key, valueStr);
        redis.delete(key);
        return;
      }

      Long postId = extractPostIdFromKey(key);
      if (postId == null) {
        log.error("postId 추출 실패, 키 삭제: {}", key);
        redis.delete(key);
        return;
      }

      // 3. DB 업데이트 (트랜잭션)
      updateDatabase(postId, delta);

      // 4. 트랜잭션 커밋 성공 후 Redis 삭제
      redis.delete(key);
      result.recordSuccess();
      log.debug("처리 완료: key={}, postId={}, delta={}", key, postId, delta);

    } catch (Exception e) {
      result.recordFailure();
      log.error("처리 실패 (다음 배치 재시도): key={}", key, e);
      // Redis 키 유지 → 다음 배치에서 재시도
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  protected void updateDatabase(Long postId, long delta) {
    // DB 업데이트만 수행 (Redis 작업 없음)
    postStatsRepository.increaseViewBy(postId, delta);
    log.debug("DB 업데이트: postId={}, delta={}", postId, delta);
  }

  private boolean tryAcquireLock() {
    Boolean acquired = redis.opsForValue()
        .setIfAbsent(LOCK_KEY, "1", 55, TimeUnit.SECONDS);
    return Boolean.TRUE.equals(acquired);
  }

  private void releaseLock() {
    redis.delete(LOCK_KEY);
  }

  private long parseLong(String value) {
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      log.warn("Long 파싱 실패: {}", value);
      return 0L;
    }
  }

  private Long extractPostIdFromKey(String key) {
    try {
      return Long.parseLong(key.substring(KEY_PREFIX.length()));
    } catch (Exception e) {
      log.error("postId 추출 실패: {}", key, e);
      return null;
    }
  }

  private static class BatchResult {

    int successCount = 0;
    int failureCount = 0;

    void recordSuccess() {
      successCount++;
    }

    void recordFailure() {
      failureCount++;
    }
  }
}
