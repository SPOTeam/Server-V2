package kr.spot.post.application.query;

import static kr.spot.post.application.query.mapper.PostResponseMapper.DEFAULT_MAX_CONTENT_LENGTH;
import static kr.spot.post.application.query.mapper.PostResponseMapper.toDetailResponse;
import static kr.spot.post.application.query.mapper.PostResponseMapper.toPostItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import kr.spot.post.domain.Comment;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.querydsl.PostQueryRepository;
import kr.spot.post.presentation.query.dto.PostDetailResponse;
import kr.spot.post.presentation.query.dto.PostListResponse;
import kr.spot.post.presentation.query.dto.PostListResponse.PostItem;
import kr.spot.study.application.validator.StudyAccessValidator;
import kr.spot.view.ViewAbuseGuard;
import kr.spot.view.ViewCounter;
import kr.spot.view.ViewableType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service(value = "studyGetPostService")
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetPostService {

  private static final int MAX_PAGE_SIZE = 50;

  private final ViewCounter viewCounter;
  private final ViewAbuseGuard viewAbuseGuard;
  private final StudyAccessValidator accessValidator;
  private final PostRepository postRepository;
  private final PostQueryRepository postQueryRepository;

  public PostDetailResponse getPostDetail(long studyId, long postId, long viewerId) {
    Post post = findAndValidatePost(studyId, postId);
    post.validatePublicAccess(accessValidator.isStudyMember(studyId, viewerId));

    PostStats stats = postQueryRepository.findStatsByPostId(postId);
    List<Comment> comments = postQueryRepository.findCommentsByPostId(postId);
    long displayViewCount = calculateDisplayViewCount(stats, postId, viewerId);

    return toDetailResponse(post, stats, displayViewCount, comments, viewerId);
  }

  public PostListResponse getPostList(long studyId, Long cursor, long viewerId, int size) {
    int pageSize = Math.min(size, MAX_PAGE_SIZE);
    boolean isStudyMember = accessValidator.isStudyMember(studyId, viewerId);

    List<Post> pinnedPosts = fetchPinnedPostsIfFirstPage(studyId, cursor);
    PaginationResult paginationResult = fetchNormalPostsWithPagination(studyId, cursor, pageSize,
        pinnedPosts.size());

    List<Post> allPosts = mergePosts(pinnedPosts, paginationResult.posts());
    Map<Long, PostStats> statsMap = fetchStatsForPosts(allPosts);

    List<PostItem> postItems = mapToPostItems(allPosts, statsMap, isStudyMember);

    return buildListResponse(postItems, paginationResult);
  }

  private Post findAndValidatePost(long studyId, long postId) {
    Post post = postRepository.getById(postId);
    post.validateBelongsToStudy(studyId);
    return post;
  }

  private List<Post> fetchPinnedPostsIfFirstPage(long studyId, Long cursor) {
    if (cursor != null) {
      return List.of();
    }
    return postQueryRepository.findPinnedPosts(studyId);
  }

  private PaginationResult fetchNormalPostsWithPagination(long studyId, Long cursor, int pageSize,
      int pinnedCount) {
    int remainingSlots = pageSize - pinnedCount;
    int fetchLimit = remainingSlots + 1;

    List<Post> posts = postQueryRepository.findPageByIdDesc(studyId, cursor, fetchLimit);
    boolean hasNext = posts.size() > remainingSlots;

    if (hasNext) {
      posts = posts.subList(0, remainingSlots);
    }

    Long nextCursor = hasNext ? posts.getLast().getId() : null;
    return new PaginationResult(posts, hasNext, nextCursor);
  }

  private List<Post> mergePosts(List<Post> pinnedPosts, List<Post> normalPosts) {
    List<Post> merged = new ArrayList<>(pinnedPosts.size() + normalPosts.size());
    merged.addAll(pinnedPosts);
    merged.addAll(normalPosts);
    return merged;
  }

  private Map<Long, PostStats> fetchStatsForPosts(List<Post> posts) {
    List<Long> postIds = posts.stream().map(Post::getId).toList();
    return postQueryRepository.findStatsByPostIds(postIds);
  }

  private List<PostItem> mapToPostItems(List<Post> posts, Map<Long, PostStats> statsMap,
      boolean isStudyMember) {
    return posts.stream()
        .map(post -> toPostItem(post, statsMap.get(post.getId()), DEFAULT_MAX_CONTENT_LENGTH,
            isStudyMember))
        .toList();
  }

  private PostListResponse buildListResponse(List<PostItem> postItems,
      PaginationResult paginationResult) {
    return PostListResponse.builder()
        .posts(postItems)
        .hasNext(paginationResult.hasNext())
        .nextCursor(paginationResult.nextCursor())
        .build();
  }

  private long calculateDisplayViewCount(PostStats stats, long postId, long viewerId) {
    long baseViewCount = (stats != null) ? stats.getViewCount() : 0L;
    long viewDelta = getViewDeltaFromCounter(postId, viewerId);
    return baseViewCount + viewDelta;
  }

  private long getViewDeltaFromCounter(long postId, long viewerId) {
    try {
      if (viewAbuseGuard.shouldCount(ViewableType.STUDY_BOARD, postId, viewerId)) {
        return viewCounter.incrementAndGet(ViewableType.STUDY_BOARD, postId);
      }
      return viewCounter.currentDelta(ViewableType.STUDY_BOARD, postId);
    } catch (Exception e) {
      log.warn("Redis view counter access failed for study postId: {}", postId, e);
      return 0L;
    }
  }

  private record PaginationResult(List<Post> posts, boolean hasNext, Long nextCursor) {

  }
}
