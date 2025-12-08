package kr.spot.infrastructure.jpa.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.QComment;
import kr.spot.domain.QPost;
import kr.spot.domain.QPostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.domain.association.QPostImage;
import kr.spot.domain.association.QPostLike;
import kr.spot.domain.enums.PostType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostQueryRepository {

  private final JPAQueryFactory query;

  public List<Post> findPageByIdDesc(PostType postType, Long cursor, int limit) {
    QPost post = QPost.post;

    return query
        .selectFrom(post)
        .where(
            ltCursor(cursor, post),
            eqType(postType, post)
        )
        .orderBy(post.id.desc())
        .limit(limit)
        .fetch();
  }

  private BooleanExpression ltCursor(Long cursor, QPost post) {
    return (cursor == null) ? null : post.id.lt(cursor);
  }

  private BooleanExpression eqType(PostType postType, QPost post) {
    return (postType == null) ? null : post.postType.eq(postType);
  }


  public Map<Long, PostStats> findStatsByPostIds(Collection<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    QPostStats ps = QPostStats.postStats;

    return query
        .selectFrom(ps)
        .where(ps.postId.in(postIds))
        .fetch()
        .stream()
        .collect(Collectors.toMap(PostStats::getPostId, it -> it));
  }

  public Map<Long, PostImage> findImagesByPostIds(Collection<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }
    QPostImage pi = QPostImage.postImage;
    return query
        .selectFrom(pi)
        .where(pi.postId.in(postIds))
        .fetch()
        .stream()
        .collect(Collectors.toMap(PostImage::getPostId, it -> it));
  }

  public Map<Long, PostStats> findStatsByPostIdsByCountQuery(Collection<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }

    QPostLike pl = QPostLike.postLike;
    QComment c = QComment.comment;

    // 1. 좋아요 개수 한 번에 조회
    List<Tuple> likeTuples = query
        .select(pl.postId, pl.id.count())
        .from(pl)
        .where(pl.postId.in(postIds))
        .groupBy(pl.postId)
        .fetch();

    Map<Long, Long> likeCountMap = likeTuples.stream()
        .collect(Collectors.toMap(
            t -> t.get(pl.postId),
            t -> t.get(pl.id.count())
        ));

    // 2. 댓글 개수 한 번에 조회
    List<Tuple> commentTuples = query
        .select(c.postId, c.id.count())
        .from(c)
        .where(c.postId.in(postIds))
        .groupBy(c.postId)
        .fetch();

    Map<Long, Long> commentCountMap = commentTuples.stream()
        .collect(Collectors.toMap(
            t -> t.get(c.postId),
            t -> t.get(c.id.count())
        ));

    // 3. 결과 합치기 (없는 건 0L 기본값)
    Map<Long, PostStats> result = new HashMap<>();
    for (Long postId : postIds) {
      long likeCount = likeCountMap.getOrDefault(postId, 0L);
      long commentCount = commentCountMap.getOrDefault(postId, 0L);

      // viewCount는 아직 없으니 0L로
      result.put(postId, PostStats.of(postId, 0L, likeCount, commentCount));
    }

    return result;
  }

  public Set<Long> findLikedPostIds(Long viewerId, Collection<Long> postIds) {
    if (viewerId == null || postIds.isEmpty()) {
      return Set.of();
    }

    QPostLike like = QPostLike.postLike;

    return new HashSet<>(
        query.select(like.postId)
            .from(like)
            .where(like.memberId.eq(viewerId),
                like.postId.in(postIds))
            .fetch()
    );
  }

  public List<Post> findLatestOnePerType() {
    QPost p = QPost.post;

    return query.selectFrom(p)
        .where(p.id.in(
            JPAExpressions
                .select(p.id.max())
                .from(p)
                .groupBy(p.postType)
        ))
        .fetch();
  }
}
