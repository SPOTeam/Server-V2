package kr.spot.post.infrastructure.batch;

import static org.mockito.Mockito.verify;

import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class StudyPostViewFlusherTest {

  @Mock
  PostStatsRepository postStatsRepository;

  StudyPostViewFlusher flusher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    flusher = new StudyPostViewFlusher(postStatsRepository);
  }

  @Test
  @DisplayName("스터디 게시글 조회수 델타를 DB에 반영한다")
  void should_update_view_count_in_db() {
    // given
    Long postId = 123L;
    long delta = 50L;

    // when
    flusher.updateViewCount(postId, delta);

    // then
    verify(postStatsRepository).increaseViewBy(postId, delta);
  }
}
