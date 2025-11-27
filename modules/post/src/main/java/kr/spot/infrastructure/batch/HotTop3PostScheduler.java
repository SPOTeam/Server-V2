package kr.spot.infrastructure.batch;

import static kr.spot.infrastructure.batch.HotPostKeyConverter.getKeyBySortType;

import java.util.List;
import kr.spot.application.ports.HotPostStore;
import kr.spot.domain.enums.SortBy;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HotTop3PostScheduler {

    private final HotPostStore store;
    private final PostStatsRepository postStatsRepository;

    @Scheduled(cron = "0 0 13,18 * * *")
    public void refreshTop3() {
        List<Long> recentTop3 = postStatsRepository.findTop3ByTotal();
        List<Long> recommendedTop3 = postStatsRepository.findTop3ByLikeCount();
        List<Long> commentedTop3 = postStatsRepository.findTop3ByCommentCount();

        store.replaceTop3(recentTop3, getKeyBySortType(SortBy.RECENT));
        store.replaceTop3(recommendedTop3, getKeyBySortType(SortBy.RECOMMEND));
        store.replaceTop3(commentedTop3, getKeyBySortType(SortBy.COMMENT_COUNT));
    }
}
