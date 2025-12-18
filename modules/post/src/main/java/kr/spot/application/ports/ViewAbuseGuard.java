package kr.spot.application.ports;

public interface ViewAbuseGuard {

  boolean shouldCount(long postId, long viewerId);
}