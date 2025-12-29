package kr.spot.post.infrastructure.batch;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyPostViewFlushJob {

  private static final String KEY_PREFIX = "view:study-board:";
  private static final String KEY_PATTERN = KEY_PREFIX + "*";
  private static final String LOCK_KEY = "lock:study-post-view-flush";

  private final StringRedisTemplate redis;
  private final StudyPostViewFlusher studyPostViewFlusher;

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
    log.info("StudyPostViewFlushJob 시작");
    long startTime = System.currentTimeMillis();

    BatchResult result = processAllKeys();

    long duration = System.currentTimeMillis() - startTime;
    log.info("StudyPostViewFlushJob 완료: 성공 {}건, 실패 {}건, {}ms",
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
          processKey(keyBytes, result);
        }
      } catch (Exception e) {
        log.error("SCAN 처리 중 오류", e);
      }
      return null;
    });

    return result;
  }

  private void processKey(byte[] keyBytes, BatchResult result) {
    String key = new String(keyBytes, StandardCharsets.UTF_8);
    long delta = 0;

    try {
      String valueStr = redis.opsForValue().getAndDelete(key);
      if (valueStr == null) {
        return;
      }

      delta = parseLong(valueStr);
      if (delta <= 0) {
        log.warn("유효하지 않은 값 무시: key={}, value={}", key, valueStr);
        return;
      }

      Long postId = extractPostIdFromKey(key);
      if (postId == null) {
        log.error("postId 추출 실패, 값 손실: key={}, delta={}", key, delta);
        return;
      }

      studyPostViewFlusher.updateViewCount(postId, delta);
      result.recordSuccess();
      log.debug("처리 완료: postId={}, delta={}", postId, delta);

    } catch (Exception e) {
      result.recordFailure();
      log.error("처리 실패: key={}", key, e);
      if (delta > 0) {
        restoreValue(key, delta);
      }
    }
  }

  private void restoreValue(String key, long delta) {
    try {
      redis.opsForValue().increment(key, delta);
      log.warn("DB 반영 실패, Redis에 값 복구 완료: key={}, delta={}", key, delta);
    } catch (Exception e) {
      log.error("복구 실패, 데이터 손실: key={}, delta={}", key, delta, e);
    }
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
