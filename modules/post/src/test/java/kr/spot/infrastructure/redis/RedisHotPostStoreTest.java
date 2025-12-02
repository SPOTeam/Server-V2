package kr.spot.infrastructure.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.spot.domain.enums.HotPostSortBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

class RedisHotPostStoreTest {

  public static final String POPULAR_TOP_3_TOTAL_RECENT = "popular:top3:total:recent";

  @Mock
  StringRedisTemplate redis;
  @Mock
  ListOperations<String, String> listOps;

  RedisHotPostStore store;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(redis.opsForList()).thenReturn(listOps);
    store = new RedisHotPostStore(redis);
  }

  @Test
  @DisplayName("replaceTop3() 호출 시 Redis에 게시글 ID 3개를 순서대로 교체 저장한다.")
  void should_replace_top3_posts_in_order() {
    // given
    List<Long> postIds = List.of(101L, 202L, 303L);

    // when
    store.replaceTop3(postIds, POPULAR_TOP_3_TOTAL_RECENT);

    // then
    verify(redis, times(1))
        .executePipelined(any(RedisCallback.class)); // 파이프라인 호출 검증
  }

  @Test
  @DisplayName("getTop3() 호출 시 Redis에서 최대 3개의 게시글 ID를 읽어와 Long 리스트로 반환한다.")
  void should_return_top3_as_long_list() {
    // given
    when(listOps.range(POPULAR_TOP_3_TOTAL_RECENT, 0, 2))
        .thenReturn(List.of("1", "2", "3"));

    // when
    List<Long> result = store.getTop3(HotPostSortBy.RECENT);

    // then
    assertThat(result).containsExactly(1L, 2L, 3L);
  }

  @Test
  @DisplayName("Redis에 값이 없을 경우 getTop3()는 빈 리스트를 반환한다.")
  void should_return_empty_list_if_no_data() {
    // given
    when(listOps.range(POPULAR_TOP_3_TOTAL_RECENT, 0, 2)).thenReturn(null);

    // when
    List<Long> result = store.getTop3(HotPostSortBy.RECENT);

    // then
    assertThat(result).isEmpty();
  }
}
