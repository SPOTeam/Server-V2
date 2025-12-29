package kr.spot.post.infrastructure.batch;

import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyPostViewFlusher {

  private final PostStatsRepository postStatsRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateViewCount(Long postId, long delta) {
    postStatsRepository.increaseViewBy(postId, delta);
    log.debug("스터디 게시글 조회수 DB 업데이트: postId={}, delta={}", postId, delta);
  }
}
