package kr.spot.post.application.command;

import static kr.spot.post.common.PostFixture.STUDY_ID;
import static kr.spot.post.common.PostFixture.post;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.post.domain.Post;
import kr.spot.post.infrastructure.jpa.PostLikeRepository;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.study.application.validator.StudyAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LikePostServiceTest {

  static final long POST_ID = 1L;
  static final long MEMBER_ID = 200L;
  static final long GENERATED_ID = 999L;

  @Mock
  IdGenerator idGenerator;

  @Mock
  PostRepository postRepository;

  @Mock
  PostLikeRepository postLikeRepository;

  @Mock
  PostStatsRepository postStatsRepository;

  @Mock
  StudyAccessValidator accessValidator;

  LikePostService likePostService;

  @BeforeEach
  void setUp() {
    likePostService = new LikePostService(
        idGenerator, postRepository, postLikeRepository, postStatsRepository, accessValidator
    );
  }

  @Nested
  @DisplayName("좋아요 추가 (likePost)")
  class LikePost {

    @Test
    @DisplayName("스터디 멤버가 게시글에 좋아요를 추가한다")
    void should_like_post_when_member() {
      // given
      Post post = post(POST_ID, STUDY_ID, MEMBER_ID);
      when(postRepository.getById(POST_ID)).thenReturn(post);
      when(idGenerator.nextId()).thenReturn(GENERATED_ID);
      when(postLikeRepository.savePostLike(GENERATED_ID, POST_ID, MEMBER_ID)).thenReturn(1);

      // when
      likePostService.likePost(STUDY_ID, POST_ID, MEMBER_ID);

      // then
      verify(accessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(postLikeRepository).savePostLike(GENERATED_ID, POST_ID, MEMBER_ID);
      verify(postStatsRepository).increaseLike(POST_ID);
    }

    @Test
    @DisplayName("이미 좋아요한 게시글에 다시 좋아요하면 카운트가 증가하지 않는다")
    void should_not_increase_like_count_when_already_liked() {
      // given
      Post post = post(POST_ID, STUDY_ID, MEMBER_ID);
      when(postRepository.getById(POST_ID)).thenReturn(post);
      when(idGenerator.nextId()).thenReturn(GENERATED_ID);
      when(postLikeRepository.savePostLike(GENERATED_ID, POST_ID, MEMBER_ID)).thenReturn(0);

      // when
      likePostService.likePost(STUDY_ID, POST_ID, MEMBER_ID);

      // then
      verify(postLikeRepository).savePostLike(GENERATED_ID, POST_ID, MEMBER_ID);
      verify(postStatsRepository, never()).increaseLike(anyLong());
    }
  }

  @Nested
  @DisplayName("좋아요 취소 (unlikePost)")
  class UnlikePost {

    @Test
    @DisplayName("좋아요를 취소하면 카운트가 감소한다")
    void should_unlike_post_and_decrease_count() {
      // given
      Post post = post(POST_ID, STUDY_ID, MEMBER_ID);
      when(postRepository.getById(POST_ID)).thenReturn(post);
      when(postLikeRepository.hardDelete(POST_ID, MEMBER_ID)).thenReturn(1);

      // when
      likePostService.unlikePost(STUDY_ID, POST_ID, MEMBER_ID);

      // then
      verify(accessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(postLikeRepository).hardDelete(POST_ID, MEMBER_ID);
      verify(postStatsRepository).decreaseLike(POST_ID);
    }

    @Test
    @DisplayName("좋아요하지 않은 게시글을 취소해도 카운트가 변경되지 않는다")
    void should_not_decrease_count_when_not_liked() {
      // given
      Post post = post(POST_ID, STUDY_ID, MEMBER_ID);
      when(postRepository.getById(POST_ID)).thenReturn(post);
      when(postLikeRepository.hardDelete(POST_ID, MEMBER_ID)).thenReturn(0);

      // when
      likePostService.unlikePost(STUDY_ID, POST_ID, MEMBER_ID);

      // then
      verify(postLikeRepository).hardDelete(POST_ID, MEMBER_ID);
      verify(postStatsRepository, never()).decreaseLike(anyLong());
    }
  }
}
