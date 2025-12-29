package kr.spot.post.application.query;

import static kr.spot.post.common.CommentFixture.comment;
import static kr.spot.post.common.PostFixture.STUDY_ID;
import static kr.spot.post.common.PostFixture.WRITER_ID;
import static kr.spot.post.common.PostFixture.pinnedPost;
import static kr.spot.post.common.PostFixture.post;
import static kr.spot.post.common.PostFixture.postStats;
import static kr.spot.post.common.PostFixture.privatePost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.post.domain.Comment;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.querydsl.PostQueryRepository;
import kr.spot.post.presentation.query.dto.PostDetailResponse;
import kr.spot.post.presentation.query.dto.PostListResponse;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetPostServiceTest {

  @Mock
  ViewCounter viewCounter;

  @Mock
  ViewAbuseGuard viewAbuseGuard;

  @Mock
  StudyAccessValidator accessValidator;

  @Mock
  PostRepository postRepository;

  @Mock
  PostQueryRepository postQueryRepository;

  GetPostService getPostService;

  @BeforeEach
  void setUp() {
    getPostService = new GetPostService(
        viewCounter, viewAbuseGuard, accessValidator, postRepository, postQueryRepository);
  }

  @Nested
  @DisplayName("게시글 상세 조회 (getPostDetail)")
  class GetPostDetail {

    @Test
    @DisplayName("게시글을 정상적으로 조회할 수 있다")
    void should_get_post_detail_successfully() {
      // given
      long postId = 1L;
      long viewerId = WRITER_ID;
      Post post = post(postId, STUDY_ID, viewerId);
      PostStats stats = postStats(postId, 10L, 5L, 3L);
      List<Comment> comments = List.of(comment(1L, postId), comment(2L, postId));

      when(postRepository.getById(postId)).thenReturn(post);
      when(postQueryRepository.findStatsByPostId(postId)).thenReturn(stats);
      when(postQueryRepository.findCommentsByPostId(postId)).thenReturn(comments);
      when(viewAbuseGuard.shouldCount(ViewableType.STUDY_BOARD, postId, viewerId)).thenReturn(true);
      when(viewCounter.incrementAndGet(ViewableType.STUDY_BOARD, postId)).thenReturn(1L);

      // when
      PostDetailResponse response = getPostService.getPostDetail(STUDY_ID, postId, viewerId);

      // then
      assertThat(response.postId()).isEqualTo(postId);
      assertThat(response.stats().viewCount()).isEqualTo(11L); // 10 + 1
      assertThat(response.stats().likeCount()).isEqualTo(5L);
      assertThat(response.comments()).hasSize(2);
      assertThat(response.isOwner()).isTrue();
    }

    @Test
    @DisplayName("다른 스터디의 게시글을 조회하면 예외가 발생한다")
    void should_throw_exception_when_post_belongs_to_different_study() {
      // given
      long postId = 1L;
      long otherStudyId = 999L;
      Post post = post(postId, STUDY_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(() -> getPostService.getPostDetail(otherStudyId, postId, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._INVALID_STUDY_ACCESS);
    }

    @Test
    @DisplayName("비공개 게시글을 조회하면 예외가 발생한다")
    void should_throw_exception_when_post_is_private() {
      // given
      long postId = 1L;
      Post post = privatePost(postId, STUDY_ID);

      when(postRepository.getById(postId)).thenReturn(post);

      // when & then
      assertThatThrownBy(() -> getPostService.getPostDetail(STUDY_ID, postId, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._PRIVATE_POST_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 게시글을 조회하면 예외가 발생한다")
    void should_throw_exception_when_post_not_found() {
      // given
      long postId = 999L;

      when(postRepository.getById(postId))
          .thenThrow(new GeneralException(ErrorStatus._POST_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> getPostService.getPostDetail(STUDY_ID, postId, WRITER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._POST_NOT_FOUND);
    }

    @Test
    @DisplayName("중복 조회 시 조회수가 증가하지 않는다")
    void should_not_increment_view_when_already_viewed() {
      // given
      long postId = 1L;
      long viewerId = WRITER_ID;
      Post post = post(postId, STUDY_ID);
      PostStats stats = postStats(postId, 10L, 5L, 3L);

      when(postRepository.getById(postId)).thenReturn(post);
      when(postQueryRepository.findStatsByPostId(postId)).thenReturn(stats);
      when(postQueryRepository.findCommentsByPostId(postId)).thenReturn(List.of());
      when(viewAbuseGuard.shouldCount(ViewableType.STUDY_BOARD, postId, viewerId)).thenReturn(false);
      when(viewCounter.currentDelta(ViewableType.STUDY_BOARD, postId)).thenReturn(5L);

      // when
      PostDetailResponse response = getPostService.getPostDetail(STUDY_ID, postId, viewerId);

      // then
      assertThat(response.stats().viewCount()).isEqualTo(15L); // 10 + 5 (기존 delta)
      verify(viewCounter, never()).incrementAndGet(any(), anyLong());
    }

    @Test
    @DisplayName("Redis 장애 시에도 기존 조회수로 정상 응답한다")
    void should_return_base_view_count_when_redis_fails() {
      // given
      long postId = 1L;
      long viewerId = WRITER_ID;
      Post post = post(postId, STUDY_ID);
      PostStats stats = postStats(postId, 10L, 5L, 3L);

      when(postRepository.getById(postId)).thenReturn(post);
      when(postQueryRepository.findStatsByPostId(postId)).thenReturn(stats);
      when(postQueryRepository.findCommentsByPostId(postId)).thenReturn(List.of());
      when(viewAbuseGuard.shouldCount(ViewableType.STUDY_BOARD, postId, viewerId))
          .thenThrow(new RuntimeException("Redis connection failed"));

      // when
      PostDetailResponse response = getPostService.getPostDetail(STUDY_ID, postId, viewerId);

      // then
      assertThat(response.stats().viewCount()).isEqualTo(10L); // 기존 조회수만
    }

    @Test
    @DisplayName("작성자가 아닌 경우 isOwner가 false이다")
    void should_return_is_owner_false_when_not_author() {
      // given
      long postId = 1L;
      long viewerId = 999L; // 다른 사용자
      Post post = post(postId, STUDY_ID, WRITER_ID);
      PostStats stats = postStats(postId);

      when(postRepository.getById(postId)).thenReturn(post);
      when(postQueryRepository.findStatsByPostId(postId)).thenReturn(stats);
      when(postQueryRepository.findCommentsByPostId(postId)).thenReturn(List.of());
      when(viewAbuseGuard.shouldCount(any(), anyLong(), anyLong())).thenReturn(false);
      when(viewCounter.currentDelta(any(), anyLong())).thenReturn(0L);

      // when
      PostDetailResponse response = getPostService.getPostDetail(STUDY_ID, postId, viewerId);

      // then
      assertThat(response.isOwner()).isFalse();
    }
  }

  @Nested
  @DisplayName("게시글 리스트 조회 (getPostList)")
  class GetPostList {

    @Test
    @DisplayName("첫 페이지에서 핀 게시글이 먼저 나온다")
    void should_show_pinned_posts_first_on_first_page() {
      // given
      Post pinned1 = pinnedPost(1L, STUDY_ID);
      Post pinned2 = pinnedPost(2L, STUDY_ID);
      Post normal1 = post(3L, STUDY_ID);
      Post normal2 = post(4L, STUDY_ID);

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of(pinned1, pinned2));
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), anyInt()))
          .thenReturn(List.of(normal1, normal2));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(1L, postStats(1L), 2L, postStats(2L), 3L, postStats(3L), 4L, postStats(4L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 10);

      // then
      assertThat(response.posts()).hasSize(4);
      assertThat(response.posts().get(0).isPinned()).isTrue();
      assertThat(response.posts().get(1).isPinned()).isTrue();
      assertThat(response.posts().get(2).isPinned()).isFalse();
      assertThat(response.posts().get(3).isPinned()).isFalse();
    }

    @Test
    @DisplayName("두 번째 페이지에서는 핀 게시글이 조회되지 않는다")
    void should_not_show_pinned_posts_on_second_page() {
      // given
      Long cursor = 3L;
      Post normal1 = post(2L, STUDY_ID);
      Post normal2 = post(1L, STUDY_ID);

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(cursor), anyInt()))
          .thenReturn(List.of(normal1, normal2));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(2L, postStats(2L), 1L, postStats(1L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, cursor, WRITER_ID, 10);

      // then
      assertThat(response.posts()).hasSize(2);
      verify(postQueryRepository, never()).findPinnedPosts(anyLong());
    }

    @Test
    @DisplayName("페이지네이션이 정상 동작한다")
    void should_handle_pagination_correctly() {
      // given
      Post post1 = post(5L, STUDY_ID);
      Post post2 = post(4L, STUDY_ID);
      Post post3 = post(3L, STUDY_ID); // 다음 페이지 존재 확인용

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), eq(3))) // size + 1
          .thenReturn(List.of(post1, post2, post3));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(5L, postStats(5L), 4L, postStats(4L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 2);

      // then
      assertThat(response.posts()).hasSize(2);
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(4L);
    }

    @Test
    @DisplayName("마지막 페이지에서는 hasNext가 false이다")
    void should_return_has_next_false_on_last_page() {
      // given
      Post post1 = post(2L, STUDY_ID);
      Post post2 = post(1L, STUDY_ID);

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), eq(11)))
          .thenReturn(List.of(post1, post2)); // size보다 적음
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(2L, postStats(2L), 1L, postStats(1L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 10);

      // then
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("스터디원인 경우 비공개 게시글이 정상 표시된다")
    void should_show_private_post_content_for_study_member() {
      // given
      Post privatePostItem = privatePost(1L, STUDY_ID);

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), anyInt()))
          .thenReturn(List.of(privatePostItem));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(1L, postStats(1L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 10);

      // then
      assertThat(response.posts().get(0).title()).isNotEqualTo("이 글은 스터디원에게만 노출됩니다.");
    }

    @Test
    @DisplayName("비스터디원인 경우 비공개 게시글이 마스킹된다")
    void should_mask_private_post_content_for_non_study_member() {
      // given
      long nonMemberId = 999L;
      Post privatePostItem = privatePost(1L, STUDY_ID);

      when(accessValidator.isStudyMember(STUDY_ID, nonMemberId)).thenReturn(false);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), anyInt()))
          .thenReturn(List.of(privatePostItem));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(1L, postStats(1L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, nonMemberId, 10);

      // then
      assertThat(response.posts().get(0).title()).isEqualTo("이 글은 스터디원에게만 노출됩니다.");
      assertThat(response.posts().get(0).content()).isEqualTo("이 글은 스터디원에게만 노출됩니다.");
    }

    @Test
    @DisplayName("게시글이 없으면 빈 리스트를 반환한다")
    void should_return_empty_list_when_no_posts() {
      // given
      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), anyInt()))
          .thenReturn(List.of());
      when(postQueryRepository.findStatsByPostIds(any())).thenReturn(Map.of());

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 10);

      // then
      assertThat(response.posts()).isEmpty();
      assertThat(response.hasNext()).isFalse();
      assertThat(response.nextCursor()).isNull();
    }

    @Test
    @DisplayName("size가 MAX_PAGE_SIZE를 초과하면 MAX_PAGE_SIZE로 제한된다")
    void should_limit_page_size_to_max() {
      // given
      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of());
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), eq(51))) // MAX_PAGE_SIZE + 1
          .thenReturn(List.of());
      when(postQueryRepository.findStatsByPostIds(any())).thenReturn(Map.of());

      // when
      getPostService.getPostList(STUDY_ID, null, WRITER_ID, 100);

      // then
      verify(postQueryRepository).findPageByIdDesc(STUDY_ID, null, 51);
    }

    @Test
    @DisplayName("핀 게시글과 일반 게시글이 함께 있을 때 올바른 페이지 크기가 적용된다")
    void should_apply_correct_page_size_with_pinned_posts() {
      // given
      Post pinned = pinnedPost(10L, STUDY_ID);
      Post normal1 = post(5L, STUDY_ID);
      Post normal2 = post(4L, STUDY_ID);
      Post normal3 = post(3L, STUDY_ID); // 다음 페이지 존재 확인용

      when(accessValidator.isStudyMember(STUDY_ID, WRITER_ID)).thenReturn(true);
      when(postQueryRepository.findPinnedPosts(STUDY_ID)).thenReturn(List.of(pinned));
      when(postQueryRepository.findPageByIdDesc(eq(STUDY_ID), eq(null), eq(3))) // size(3) - pinned(1) + 1
          .thenReturn(List.of(normal1, normal2, normal3));
      when(postQueryRepository.findStatsByPostIds(any()))
          .thenReturn(Map.of(10L, postStats(10L), 5L, postStats(5L), 4L, postStats(4L)));

      // when
      PostListResponse response = getPostService.getPostList(STUDY_ID, null, WRITER_ID, 3);

      // then
      assertThat(response.posts()).hasSize(3); // pinned(1) + normal(2)
      assertThat(response.hasNext()).isTrue();
      assertThat(response.nextCursor()).isEqualTo(4L);
    }
  }
}
