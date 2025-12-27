package kr.spot.view;

public interface ViewAbuseGuard {

    boolean shouldCount(ViewableType type, long targetId, long viewerId);
}
