package kr.spot.study.application.query;

import kr.spot.study.domain.associations.StudyStats;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyViewCountService {

  private final ViewCounter viewCounter;
  private final ViewAbuseGuard viewAbuseGuard;

  public long calculateDisplayViewCount(StudyStats stats, long studyId, long viewerId) {
    long baseViewCount = stats.getViewCount();
    long viewDelta = getViewDeltaFromCounter(studyId, viewerId);
    return baseViewCount + viewDelta;
  }

  private long getViewDeltaFromCounter(long studyId, long viewerId) {
    try {
      if (viewAbuseGuard.shouldCount(ViewableType.STUDY, studyId, viewerId)) {
        return viewCounter.incrementAndGet(ViewableType.STUDY, studyId);
      }
      return viewCounter.currentDelta(ViewableType.STUDY, studyId);
    } catch (Exception e) {
      log.warn("Redis view counter access failed for studyId: {}", studyId, e);
      return 0L;
    }
  }
}
