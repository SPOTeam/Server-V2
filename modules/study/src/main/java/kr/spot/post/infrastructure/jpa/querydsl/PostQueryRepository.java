package kr.spot.post.infrastructure.jpa.querydsl;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.domain.QComment;
import kr.spot.post.domain.QPost;
import kr.spot.post.domain.QPostStats;
import kr.spot.post.domain.Comment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository(value = "studyPostQueryRepository")
@RequiredArgsConstructor
public class PostQueryRepository {

  private final JPAQueryFactory query;

  private final QPost post = QPost.post;
  private final QPostStats postStats = QPostStats.postStats;
  private final QComment comment = QComment.comment;

  public List<Post> findPinnedPosts(long studyId) {
    return query
        .selectFrom(post)
        .where(
            post.studyId.eq(studyId),
            post.pinnedAt.isNotNull()
        )
        .orderBy(post.pinnedAt.desc())
        .fetch();
  }

  public List<Post> findPageByIdDesc(long studyId, Long cursor, int limit) {
    return query
        .selectFrom(post)
        .where(
            post.studyId.eq(studyId),
            post.pinnedAt.isNull(),
            ltCursor(cursor)
        )
        .orderBy(post.id.desc())
        .limit(limit)
        .fetch();
  }

  private BooleanExpression ltCursor(Long cursor) {
    return (cursor == null) ? null : post.id.lt(cursor);
  }

  public Map<Long, PostStats> findStatsByPostIds(Collection<Long> postIds) {
    if (postIds.isEmpty()) {
      return Map.of();
    }

    return query
        .selectFrom(postStats)
        .where(postStats.postId.in(postIds))
        .fetch()
        .stream()
        .collect(Collectors.toMap(PostStats::getPostId, it -> it));
  }

  public PostStats findStatsByPostId(long postId) {
    return query
        .selectFrom(postStats)
        .where(postStats.postId.eq(postId))
        .fetchOne();
  }

  public List<Comment> findCommentsByPostId(long postId) {
    return query
        .selectFrom(comment)
        .where(comment.postId.eq(postId))
        .orderBy(comment.createdAt.asc())
        .fetch();
  }
}
