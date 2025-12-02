package kr.spot.infrastructure.jpa.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.QPost;
import kr.spot.domain.QPostStats;
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
