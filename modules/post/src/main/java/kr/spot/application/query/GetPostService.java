package kr.spot.application.query;

import static kr.spot.application.query.mapper.PostResponseMapper.toPostDetail;
import static kr.spot.application.query.mapper.PostResponseMapper.toPostList;
import static kr.spot.application.query.mapper.PostResponseMapper.toPostOverview;
import static kr.spot.application.query.mapper.PostResponseMapper.toRecentPost;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import kr.spot.application.ports.HotPostStore;
import kr.spot.application.ports.PostViewCounter;
import kr.spot.application.ports.ViewAbuseGuard;
import kr.spot.domain.Comment;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.domain.enums.HotPostSortBy;
import kr.spot.domain.enums.PostType;
import kr.spot.infrastructure.jpa.CommentRepository;
import kr.spot.infrastructure.jpa.PostImageRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.infrastructure.jpa.querydsl.PostQueryRepository;
import kr.spot.presentation.query.dto.response.PostDetailResponse;
import kr.spot.presentation.query.dto.response.PostListResponse;
import kr.spot.presentation.query.dto.response.PostListResponse.PostList;
import kr.spot.presentation.query.dto.response.PostOverviewResponse;
import kr.spot.presentation.query.dto.response.PostOverviewResponse.PostOverview;
import kr.spot.presentation.query.dto.response.RecentPostResponse;
import kr.spot.presentation.query.dto.response.RecentPostResponse.RecentPost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPostService {

  public static final int MAX_PAGE_SIZE = 50;
  public static final int MAX_CONTENT_LENGTH = 100;

  private final PostViewCounter postViewCounter;
  private final ViewAbuseGuard redisViewAbuseGuard; // 임시 조치
  private final HotPostStore hotPostStore;
  private final PostRepository postRepository;
  private final PostQueryRepository postQueryRepository;
  private final CommentRepository commentRepository;
  private final PostStatsRepository postStatsRepository;
  private final PostImageRepository postImageRepository;

  /**
   * 게시글 목록을 커서 기반 페이지네이션으로 조회합니다.
   *
   * @param postType 게시글 유형
   * @param cursor   이전 페이지의 마지막 게시글 ID (다음 페이지 조회를 위한 커서)
   * @param viewerId 현재 조회자 ID
   * @param size     요청 페이지 크기
   * @return 게시글 목록 및 페이지네이션 정보
   */
  public PostListResponse getPostList(PostType postType, Long cursor, Long viewerId, Integer size) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);

    List<Post> rows = postQueryRepository.findPageByIdDesc(postType, cursor, pageSize + 1);
    boolean hasNext = rows.size() > pageSize;
    if (hasNext) {
      rows = rows.subList(0, pageSize);
    }
    Long nextCursor = hasNext ? rows.getLast().getId() : null;

    List<Long> ids = rows.stream().map(Post::getId).toList();
    Map<Long, PostStats> stats = postQueryRepository.findStatsByPostIds(ids);
    Set<Long> liked = postQueryRepository.findLikedPostIds(viewerId, ids);

    List<PostList> posts = rows.stream()
        .map(p -> toPostList(p, stats.get(p.getId()), liked.contains(p.getId())))
        .toList();

    return PostListResponse.builder()
        .posts(posts)
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .build();
  }

  /**
   * 게시글 목록을 커서 기반 페이지네이션으로 조회합니다.
   *
   * @param postType 게시글 유형
   * @param cursor   이전 페이지의 마지막 게시글 ID (다음 페이지 조회를 위한 커서)
   * @param viewerId 현재 조회자 ID
   * @param size     요청 페이지 크기
   * @return 게시글 목록 및 페이지네이션 정보
   */
  public PostListResponse getPostListByCountQuery(PostType postType, Long cursor, Long viewerId,
      Integer size) {
    final int pageSize = Math.min(size, MAX_PAGE_SIZE);

    List<Post> rows = postQueryRepository.findPageByIdDesc(postType, cursor, pageSize + 1);
    boolean hasNext = rows.size() > pageSize;
    if (hasNext) {
      rows = rows.subList(0, pageSize);
    }
    Long nextCursor = hasNext ? rows.getLast().getId() : null;

    List<Long> ids = rows.stream().map(Post::getId).toList();
    Map<Long, PostStats> stats = postQueryRepository.findStatsByPostIdsByCountQuery(ids);
    Set<Long> liked = postQueryRepository.findLikedPostIds(viewerId, ids);

    List<PostList> posts = rows.stream()
        .map(p -> toPostList(p, stats.get(p.getId()), liked.contains(p.getId())))
        .toList();

    return PostListResponse.builder()
        .posts(posts)
        .hasNext(hasNext)
        .nextCursor(nextCursor)
        .build();
  }

  /**
   * 특정 게시글의 상세 정보를 조회합니다.
   *
   * @param postId   게시글 ID
   * @param viewerId 현재 조회자 ID
   * @return 게시글 상세 정보 (Post, PostStats, Comments 포함)
   */
  public PostDetailResponse getPostDetail(Long postId, Long viewerId) {
    Post post = postRepository.getPostById(postId);
    PostStats postStats = postStatsRepository.getPostStatsById(postId);
    PostImage postImage = postImageRepository.getPostImageById(postId);
    List<Comment> comments = commentRepository.getCommentsByPostId(postId);

    long displayView = postStats.getViewCount() + getViewDeltaFromCounter(postId, viewerId);
    return toPostDetail(post, postStats, postImage, displayView, comments);
  }


  /**
   * 인기 게시글 상위 3개를 조회합니다.
   *
   * @return 인기 게시글 3개의 개요 정보
   */
  public PostOverviewResponse getHotPosts(HotPostSortBy sortBy) {
    List<Long> top3 = hotPostStore.getTop3(sortBy);
    if (top3.isEmpty()) {
      return PostOverviewResponse.of(List.of());
    }

    List<Post> posts = postRepository.getPostsByIds(top3);
    Map<Long, PostStats> stats = postQueryRepository.findStatsByPostIds(top3);

    List<PostOverview> hotPosts = posts.stream()
        .map(p -> toPostOverview(p, stats.get(p.getId())))
        .toList();

    return PostOverviewResponse.of(hotPosts);
  }

  /**
   * 게시글 유형별 최신 게시글을 조회합니다.
   *
   * @return 게시글 유형별 최신 게시글 정보
   */
  public RecentPostResponse getRecentPosts() {
    List<Post> latest = postQueryRepository.findLatestOnePerType();
    if (latest.isEmpty()) {
      return RecentPostResponse.of(List.of());
    }

    List<Long> ids = latest.stream().map(Post::getId).toList();
    Map<Long, PostStats> statsMap = postQueryRepository.findStatsByPostIds(ids);

    List<RecentPost> items = latest.stream()
        .map(p -> toRecentPost(p, statsMap.get(p.getId())))
        .sorted(Comparator.comparing(RecentPost::postType))
        .toList();

    return RecentPostResponse.of(items);
  }


  private long getViewDeltaFromCounter(Long postId, Long viewerId) {
    long viewDelta = 0L;
    try {
      if (redisViewAbuseGuard.shouldCount(postId, viewerId)) {
        viewDelta = postViewCounter.incrementAndGetDelta(postId); // 델타 증가 및 현재값 반환
      } else {
        viewDelta = postViewCounter.currentDelta(postId); // 델타만 조회
      }
    } catch (Exception ignore) {
      // Redis 장애 시에도 조회가 가능하도록 예외 무시: 표시값은 DB 기준
      log.warn("Redis view counter access failed for postId: {}", postId, ignore);
    }
    return viewDelta;
  }
}
