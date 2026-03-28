package kr.spot.study.infrastructure.batch;

import static org.mockito.Mockito.verify;

import kr.spot.study.infrastructure.jpa.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StudyViewFlusherTest {

  @Mock
  StudyRepository studyRepository;

  StudyViewFlusher flusher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    flusher = new StudyViewFlusher(studyRepository);
  }

  @Test
  @DisplayName("스터디 조회수 델타를 DB에 반영한다")
  void should_update_view_count_in_db() {
    // given
    long studyId = 123L;
    long delta = 50L;

    // when
    flusher.updateViewCount(studyId, delta);

    // then
    verify(studyRepository).increaseViewBy(studyId, delta);
  }
}
