package kr.spot.view.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisViewCounterTest {

  private static final String DELTA_KEY_FORMAT = "view:%s:%d";

  @Mock
  StringRedisTemplate redis;

  @Mock
  ValueOperations<String, String> valueOps;

  ViewCounter counter;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redis.opsForValue()).thenReturn(valueOps);
    counter = new RedisViewCounter(redis);
  }

  @Test
  @DisplayName("조회수 델타를 증가시키고, 증가된 값을 반환한다.")
  void should_increment_and_get_delta() {
    // given
    long targetId = 1L;
    String key = DELTA_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId);
    when(valueOps.increment(key)).thenReturn(5L);

    // when
    long delta = counter.incrementAndGet(ViewableType.POST, targetId);

    // then
    assertThat(delta).isEqualTo(5L);
  }

  @Test
  @DisplayName("델타 값 증가 시, 키에 해당하는 값이 없으면 0을 반환한다.")
  void should_return_0_if_no_value() {
    // given
    long targetId = 1L;
    String key = DELTA_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId);
    when(valueOps.increment(key)).thenReturn(null);

    // when
    long delta = counter.incrementAndGet(ViewableType.POST, targetId);

    // then
    assertThat(delta).isEqualTo(0L);
  }

  @Test
  @DisplayName("키에 해당하는 값을 long 타입으로 변환하여 반환한다.")
  void should_return_current_delta() {
    // given
    long targetId = 1L;
    String key = DELTA_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId);
    when(valueOps.get(key)).thenReturn("10");

    // when
    long delta = counter.currentDelta(ViewableType.POST, targetId);

    // then
    assertThat(delta).isEqualTo(10L);
  }

  @Test
  @DisplayName("현재 델타 값 조회 시, 키에 해당하는 값이 없으면 0을 반환한다.")
  void should_return_0_if_no_current_value() {
    // given
    long targetId = 1L;
    String key = DELTA_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId);
    when(valueOps.get(key)).thenReturn(null);

    // when
    long delta = counter.currentDelta(ViewableType.POST, targetId);

    // then
    assertThat(delta).isEqualTo(0L);
  }

  @Test
  @DisplayName("STUDY 타입에 대해서도 정상적으로 동작한다.")
  void should_work_for_study_type() {
    // given
    long studyId = 100L;
    String key = DELTA_KEY_FORMAT.formatted(ViewableType.STUDY.getKeyPrefix(), studyId);
    when(valueOps.increment(key)).thenReturn(3L);

    // when
    long delta = counter.incrementAndGet(ViewableType.STUDY, studyId);

    // then
    assertThat(delta).isEqualTo(3L);
  }
}
