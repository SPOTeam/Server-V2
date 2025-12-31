package kr.spot.study.infrastructure.batch;

import kr.spot.study.infrastructure.jpa.associations.StudyStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyViewFlusher {

  private final StudyStatsRepository studyStatsRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateViewCount(long studyId, long delta) {
    studyStatsRepository.increaseViewBy(studyId, delta);
    log.debug("스터디 조회수 DB 업데이트: studyId={}, delta={}", studyId, delta);
  }
}
