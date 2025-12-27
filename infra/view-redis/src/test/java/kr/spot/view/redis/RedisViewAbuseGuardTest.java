package kr.spot.view.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Duration;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

class RedisViewAbuseGuardTest {

  private static final Duration TIMEOUT = Duration.ofMinutes(10);
  private static final String GUARD_KEY_FORMAT = "view:guard:%s:%d:%d";

  @Mock
  StringRedisTemplate redis;

  @Mock
  ValueOperations<String, String> valueOps;

  ViewAbuseGuard guard;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redis.opsForValue()).thenReturn(valueOps);
    guard = new RedisViewAbuseGuard(redis);
  }

  @Test
  @DisplayName("이전에 조회한 적 없는 사용자는 true를 반환한다.")
  void should_return_true_for_new_viewer() {
    // given
    long targetId = 1L;
    long viewerId = 2L;
    String key = GUARD_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId, viewerId);

    when(valueOps.setIfAbsent(key, "1", TIMEOUT)).thenReturn(true);

    // when
    boolean result = guard.shouldCount(ViewableType.POST, targetId, viewerId);

    // then
    assertThat(result).isTrue();
  }

  @Test
  @DisplayName("이전에 조회한 적 있는 사용자는 false를 반환한다.")
  void should_return_false_for_existing_viewer() {
    // given
    long targetId = 1L;
    long viewerId = 2L;
    String key = GUARD_KEY_FORMAT.formatted(ViewableType.POST.getKeyPrefix(), targetId, viewerId);

    when(valueOps.setIfAbsent(key, "1", TIMEOUT)).thenReturn(false);

    // when
    boolean result = guard.shouldCount(ViewableType.POST, targetId, viewerId);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("STUDY 타입에 대해서도 정상적으로 동작한다.")
  void should_work_for_study_type() {
    // given
    long studyId = 100L;
    long viewerId = 2L;
    String key = GUARD_KEY_FORMAT.formatted(ViewableType.STUDY.getKeyPrefix(), studyId, viewerId);

    when(valueOps.setIfAbsent(key, "1", TIMEOUT)).thenReturn(true);

    // when
    boolean result = guard.shouldCount(ViewableType.STUDY, studyId, viewerId);

    // then
    assertThat(result).isTrue();
  }
}
