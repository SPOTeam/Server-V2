package kr.spot.ports.dto;

public record EnsureResult(long memberId, boolean isNew) {

  public static EnsureResult of(long memberId, boolean isNew) {
    return new EnsureResult(memberId, isNew);
  }
}
