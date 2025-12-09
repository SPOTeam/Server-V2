package kr.spot.fake;

import kr.spot.IdGenerator;
import java.util.concurrent.atomic.AtomicLong;

public class FakeIdGenerator implements IdGenerator {

  private final AtomicLong counter;

  public FakeIdGenerator() {
    this.counter = new AtomicLong(1L);
  }

  public FakeIdGenerator(long startValue) {
    this.counter = new AtomicLong(startValue);
  }

  @Override
  public long nextId() {
    return counter.getAndIncrement();
  }
}