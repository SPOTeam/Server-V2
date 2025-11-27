package kr.spot.infrastructure.redis;

import static kr.spot.infrastructure.batch.HotPostKeyConverter.getKeyBySortType;

import java.util.List;
import kr.spot.application.ports.HotPostStore;
import kr.spot.domain.enums.SortBy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisHotPostStore implements HotPostStore {

    public static final int START = 0;
    public static final int END = 2;

    private final StringRedisTemplate redis;

    @Override
    public void replaceTop3(List<Long> postIds, String key) {
        redis.executePipelined(
                (RedisCallback<Object>) conn -> {
                    conn.del(key.getBytes());
                    for (Long id : postIds) {
                        conn.rPush(key.getBytes(), String.valueOf(id).getBytes());
                    }
                    return null;
                });
    }

    @Override
    public List<Long> getTop3(SortBy sortBy) {
        List<String> vals = redis.opsForList().range(getKeyBySortType(sortBy), START, END);
        if (vals == null) {
            return List.of();
        }
        return vals.stream()
                .map(Long::valueOf)
                .toList();
    }
}
