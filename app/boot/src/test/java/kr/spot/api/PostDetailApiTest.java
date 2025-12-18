package kr.spot.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@Disabled
public class PostDetailApiTest {

  RestClient restClient = RestClient.create("http://localhost:8080");

  @Test
  void viewTest() throws InterruptedException {
    int totalRequests = 100000;
    ExecutorService executorService = Executors.newFixedThreadPool(100);
    CountDownLatch latch = new CountDownLatch(totalRequests);

    List<Long> latencies = Collections.synchronizedList(new ArrayList<>());

    long start = System.currentTimeMillis();

    for (int i = 0; i < totalRequests; i++) {
      executorService.submit(() -> {
        long t0 = System.nanoTime();
        try {
          restClient.get()
              .uri("/api/posts/{postId}", 226638045626925056L)
              .retrieve()
              .toBodilessEntity();
          long t1 = System.nanoTime();
          latencies.add((t1 - t0) / 1_000_000); // ms 단위
        } catch (Exception e) {
          // 실패한 요청도 기록 가능
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();
    long end = System.currentTimeMillis();

    executorService.shutdown();

    System.out.println("총 실행 시간 = " + (end - start) + " ms");
    System.out.println(
        "평균 응답 시간 = " + latencies.stream().mapToLong(Long::longValue).average().orElse(0) + " ms");

    latencies.sort(Long::compare);
    int idx95 = (int) Math.ceil(latencies.size() * 0.95) - 1;
    int idx99 = (int) Math.ceil(latencies.size() * 0.99) - 1;

    System.out.println("P95 = " + latencies.get(Math.max(idx95, 0)) + " ms");
    System.out.println("P99 = " + latencies.get(Math.max(idx99, 0)) + " ms");
  }
}
