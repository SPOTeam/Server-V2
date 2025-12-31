package kr.spot.study.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import kr.spot.study.domain.associations.StudyStats;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudyViewCountService 단위 테스트")
class StudyViewCountServiceTest {

  @Mock
  ViewCounter viewCounter;

  @Mock
  ViewAbuseGuard viewAbuseGuard;

  @InjectMocks
  StudyViewCountService sut;

  @Nested
  @DisplayName("calculateDisplayViewCount 메서드는")
  class CalculateDisplayViewCountTest {

    private final long studyId = 1L;
    private final long viewerId = 100L;

    @Test
    @DisplayName("새로운 조회일 경우 조회수를 증가시키고 합산하여 반환한다")
    void should_increment_and_return_total_when_new_view() {
      // given
      StudyStats stats = StudyStats.of(studyId);
      long expectedDelta = 5L;

      given(viewAbuseGuard.shouldCount(ViewableType.STUDY, studyId, viewerId))
          .willReturn(true);
      given(viewCounter.incrementAndGet(ViewableType.STUDY, studyId))
          .willReturn(expectedDelta);

      // when
      long result = sut.calculateDisplayViewCount(stats, studyId, viewerId);

      // then
      assertThat(result).isEqualTo(expectedDelta);
      verify(viewCounter).incrementAndGet(ViewableType.STUDY, studyId);
    }

    @Test
    @DisplayName("중복 조회일 경우 증가 없이 현재 델타만 반환한다")
    void should_return_current_delta_when_duplicate_view() {
      // given
      StudyStats stats = StudyStats.of(studyId);
      long currentDelta = 10L;

      given(viewAbuseGuard.shouldCount(ViewableType.STUDY, studyId, viewerId))
          .willReturn(false);
      given(viewCounter.currentDelta(ViewableType.STUDY, studyId))
          .willReturn(currentDelta);

      // when
      long result = sut.calculateDisplayViewCount(stats, studyId, viewerId);

      // then
      assertThat(result).isEqualTo(currentDelta);
      verify(viewCounter, never()).incrementAndGet(ViewableType.STUDY, studyId);
      verify(viewCounter).currentDelta(ViewableType.STUDY, studyId);
    }

    @Test
    @DisplayName("Redis 오류 발생 시 기본 조회수만 반환한다")
    void should_return_base_count_when_redis_error() {
      // given
      StudyStats stats = StudyStats.of(studyId);

      given(viewAbuseGuard.shouldCount(ViewableType.STUDY, studyId, viewerId))
          .willThrow(new RuntimeException("Redis connection failed"));

      // when
      long result = sut.calculateDisplayViewCount(stats, studyId, viewerId);

      // then
      assertThat(result).isEqualTo(stats.getViewCount());
    }
  }
}
