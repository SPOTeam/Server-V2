package kr.spot.infrastructure.batch;


import kr.spot.domain.enums.HotPostSortBy;

public class HotPostKeyConverter {
    private static final String RECENT_KEY = "popular:top3:total:recent";
    private static final String RECOMMENDED_KEY = "popular:top3:like:recommended";
    private static final String COMMENTED_KEY = "popular:top3:comment:commented";

    public static String getKeyBySortType(HotPostSortBy sortBy) {
        return switch (sortBy) {
            case RECENT -> RECENT_KEY;
            case RECOMMEND -> RECOMMENDED_KEY;
            case COMMENT_COUNT -> COMMENTED_KEY;
        };
    }
}
