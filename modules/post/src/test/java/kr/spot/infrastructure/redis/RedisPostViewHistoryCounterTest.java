package kr.spot.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import kr.spot.application.ports.PostViewCounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisPostViewHistoryCounterTest {

  @Mock
  StringRedisTemplate redis;

  @Mock
  ValueOperations<String, String> valueOps;

  PostViewCounter counter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redis.opsForValue()).thenReturn(valueOps);
    counter = new RedisPostViewCounter(redis);
  }

  @Test
  @DisplayName("게시글의 조회수 델타를 증가시키고, 증가된 값을 반환한다.")
  void should_increment_and_get_delta() {
    // given
    long postId = 1L;
    String key = "view:post:%d".formatted(postId);
    when(valueOps.increment(key)).thenReturn(5L);

    // when
    long delta = counter.incrementAndGetDelta(postId);

    // then
    assertThat(delta).isEqualTo(5L);
  }

  @Test
  @DisplayName("델타 값 증가 시, 키에 해당하는 값이 없으면 0을 반환한다.")
  void should_return_0_if_no_value() {
    // given
    long postId = 1L;
    String key = "view:post:%d".formatted(postId);
    when(valueOps.get(key)).thenReturn(null);

    // when
    long delta = counter.incrementAndGetDelta(postId);

    // then
    assertThat(delta).isEqualTo(0L);
  }

  @Test
  @DisplayName("키에 해당하는 값을 long 타입으로 변환하여 반환한다.")
  void should_return_current_delta() {
    // given
    long postId = 1L;
    String key = "view:post:%d".formatted(postId);
    when(valueOps.get(key)).thenReturn("10");

    // when
    long delta = counter.currentDelta(postId);

    // then
    assertThat(delta).isEqualTo(10L);
  }

  @Test
  @DisplayName("현재 델타 값 조회 시, 키에 해당하는 값이 없으면 0을 반환한다.")
  void should_return_0_if_no_current_value() {
    // given
    long postId = 1L;
    String key = "view:post:%d".formatted(postId);
    when(valueOps.get(key)).thenReturn(null);

    // when
    long delta = counter.currentDelta(postId);

    // then
    assertThat(delta).isEqualTo(0L);
  }
}
