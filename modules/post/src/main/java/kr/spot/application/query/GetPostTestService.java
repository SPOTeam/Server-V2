package kr.spot.application.query;

import static kr.spot.application.query.mapper.PostResponseMapper.toPostDetail;

import java.util.List;
import kr.spot.application.ports.PostViewCounter;
import kr.spot.domain.Comment;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.infrastructure.jpa.CommentRepository;
import kr.spot.infrastructure.jpa.PostImageRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.infrastructure.rdb.DbViewAbuseGuard;
import kr.spot.infrastructure.redis.RedisViewAbuseGuard;
import kr.spot.presentation.query.dto.response.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class GetPostTestService {

  private final PostViewCounter postViewCounter;
  private final DbViewAbuseGuard dbViewAbuseGuard;
  private final RedisViewAbuseGuard redisViewAbuseGuard;

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final PostStatsRepository postStatsRepository;
  private final PostImageRepository postImageRepository;


  @Transactional(readOnly = true)
  public PostDetailResponse getPostDetailByRedis(Long postId, Long viewerId) {
    Post post = postRepository.getPostById(postId);
    PostStats postStats = postStatsRepository.getPostStatsById(postId);
    PostImage postImage = postImageRepository.findById(postId).orElse(PostImage.of(postId, null));
    List<Comment> comments = commentRepository.getCommentsByPostId(postId);

    long displayView = postStats.getViewCount() + getViewDeltaFromCounterByRedis(postId, viewerId);
    return toPostDetail(post, postStats, postImage, displayView, comments);
  }

  private long getViewDeltaFromCounterByRedis(Long postId, Long viewerId) {
    long viewDelta = 0L;
    try {
      if (redisViewAbuseGuard.shouldCount(postId, viewerId)) {
        viewDelta = postViewCounter.incrementAndGetDelta(postId); // 델타 증가 및 현재값 반환
      } else {
        viewDelta = postViewCounter.currentDelta(postId); // 델타만 조회
      }
    } catch (Exception ignore) {
      log.warn("Redis view counter access failed for postId: {}", postId, ignore);
    }
    return viewDelta;
  }

  public PostDetailResponse getPostDetailByRDB(Long postId, Long viewerId) {
    Post post = postRepository.getPostById(postId);
    PostStats postStats = postStatsRepository.getPostStatsById(postId);
    PostImage postImage = postImageRepository.findById(postId)
        .orElse(PostImage.of(postId, null));
    List<Comment> comments = commentRepository.getCommentsByPostId(postId);

    boolean counted = increaseViewCountIfNeeded(postId, viewerId);
    long displayView = postStats.getViewCount() + (counted ? 1 : 0);

    return toPostDetail(post, postStats, postImage, displayView, comments);
  }

  private boolean increaseViewCountIfNeeded(Long postId, Long viewerId) {
    try {
      if (dbViewAbuseGuard.shouldCount(postId, viewerId)) {
        postStatsRepository.increaseViewBy(postId, 1);
        return true;
      }
    } catch (Exception e) {
      log.warn("RDB view counter access failed for postId: {}", postId, e);
    }
    return false;
  }
}
