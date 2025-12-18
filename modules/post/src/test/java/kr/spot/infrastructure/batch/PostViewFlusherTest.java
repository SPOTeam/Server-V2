package kr.spot.infrastructure.batch;

import static org.mockito.Mockito.verify;

import kr.spot.infrastructure.jpa.PostStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class PostViewFlusherTest {

  @Mock
  PostStatsRepository postStatsRepository;

  PostViewFlusher flusher;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    flusher = new PostViewFlusher(postStatsRepository);
  }

  @Test
  @DisplayName("조회수 델타를 DB에 반영한다")
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
