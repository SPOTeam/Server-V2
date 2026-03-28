package kr.spot.view;

public interface ViewCounter {

    long incrementAndGet(ViewableType type, long targetId);

    long currentDelta(ViewableType type, long targetId);
}
