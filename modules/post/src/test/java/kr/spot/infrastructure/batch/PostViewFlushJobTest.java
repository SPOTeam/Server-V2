package kr.spot.infrastructure.batch;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class PostViewFlushJobTest {

  @Mock
  StringRedisTemplate redis;

  @Mock
  ValueOperations<String, String> valueOps;

  @Mock
  PostViewFlusher postViewFlusher;

  PostViewFlushJob job;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redis.opsForValue()).thenReturn(valueOps);
    job = new PostViewFlushJob(redis, postViewFlusher);
  }

  @Nested
  @DisplayName("분산 락 테스트")
  class DistributedLockTest {

    @Test
    @DisplayName("락 획득 실패 시 배치를 실행하지 않는다")
    void should_not_execute_when_lock_not_acquired() {
      // given
      when(valueOps.setIfAbsent(eq("lock:view-flush"), eq("1"), eq(55L), eq(TimeUnit.SECONDS)))
          .thenReturn(false);

      // when
      job.flush();

      // then
      verify(redis, never()).execute(any(RedisCallback.class));
    }

    @Test
    @DisplayName("락 획득 성공 시 배치를 실행하고 락을 해제한다")
    void should_execute_and_release_lock_when_acquired() {
      // given
      when(valueOps.setIfAbsent(eq("lock:view-flush"), eq("1"), eq(55L), eq(TimeUnit.SECONDS)))
          .thenReturn(true);

      // when
      job.flush();

      // then
      verify(redis).execute(any(RedisCallback.class));
      verify(redis).delete("lock:view-flush");
    }

    @Test
    @DisplayName("배치 실행 중 예외가 발생해도 락을 해제한다")
    void should_release_lock_even_when_exception_occurs() {
      // given
      when(valueOps.setIfAbsent(eq("lock:view-flush"), eq("1"), eq(55L), eq(TimeUnit.SECONDS)))
          .thenReturn(true);
      when(redis.execute(any(RedisCallback.class)))
          .thenThrow(new RuntimeException("Redis error"));

      // when
      try {
        job.flush();
      } catch (Exception ignored) {
      }

      // then
      verify(redis).delete("lock:view-flush");
    }
  }
}
