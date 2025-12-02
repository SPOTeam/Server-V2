package kr.spot.application.ports;

public interface PostViewCounter {

  long incrementAndGetDelta(long postId);

  long currentDelta(long postId);
}
