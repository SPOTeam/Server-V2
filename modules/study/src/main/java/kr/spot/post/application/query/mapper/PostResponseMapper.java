package kr.spot.post.application.query.mapper;

import java.util.List;
import kr.spot.post.domain.Comment;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.domain.vo.WriterInfo;
import kr.spot.post.presentation.query.dto.PostDetailResponse;
import kr.spot.post.presentation.query.dto.PostDetailResponse.CommentResponse;
import kr.spot.post.presentation.query.dto.PostListResponse.PostItem;
import kr.spot.post.presentation.query.dto.PostListResponse.WriterInfoResponse;
import kr.spot.post.presentation.query.dto.PostStatsResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostResponseMapper {

  public static final int DEFAULT_MAX_CONTENT_LENGTH = 100;
  private static final String PRIVATE_POST_MESSAGE = "이 글은 스터디원에게만 노출됩니다.";

  public static PostDetailResponse toDetailResponse(
      Post post,
      PostStats stats,
      long displayViewCount,
      List<Comment> comments,
      long viewerId,
      boolean isLiked
  ) {
    return PostDetailResponse.builder()
        .postId(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .isPinned(post.isPinned())
        .isOwner(post.isOwnedBy(viewerId))
        .isLiked(isLiked)
        .writer(toDetailWriterInfoResponse(post.getWriterInfo()))
        .stats(toStatsResponse(stats, displayViewCount))
        .createdAt(post.getCreatedAt())
        .comments(toCommentResponses(comments, viewerId))
        .build();
  }

  public static PostItem toPostItem(Post post, PostStats stats, int maxContentLength,
      boolean isStudyMember, boolean isLiked) {
    if (post.isPrivate() && !isStudyMember) {
      return toMaskedPostItem(post, stats, isLiked);
    }
    return toPublicPostItem(post, stats, maxContentLength, isLiked);
  }

  private static PostItem toPublicPostItem(Post post, PostStats stats, int maxContentLength,
      boolean isLiked) {
    return PostItem.of(
        post.getId(),
        post.getTitle(),
        summarize(post.getContent(), maxContentLength),
        post.isPinned(),
        isLiked,
        toStatsResponse(stats),
        toWriterInfoResponse(post.getWriterInfo()),
        post.getCreatedAt()
    );
  }

  private static PostItem toMaskedPostItem(Post post, PostStats stats, boolean isLiked) {
    return PostItem.of(
        post.getId(),
        PRIVATE_POST_MESSAGE,
        PRIVATE_POST_MESSAGE,
        post.isPinned(),
        isLiked,
        toStatsResponse(stats),
        toWriterInfoResponse(post.getWriterInfo()),
        post.getCreatedAt()
    );
  }

  private static List<CommentResponse> toCommentResponses(List<Comment> comments, long viewerId) {
    return comments.stream()
        .map(comment -> toCommentResponse(comment, viewerId))
        .toList();
  }

  private static CommentResponse toCommentResponse(Comment comment, long viewerId) {
    WriterInfo writer = comment.getWriterInfo();
    return CommentResponse.of(
        comment.getId(),
        comment.getContent(),
        writer.isSameWriter(viewerId),
        toDetailWriterInfoResponse(writer),
        comment.getCreatedAt()
    );
  }

  private static WriterInfoResponse toWriterInfoResponse(WriterInfo writerInfo) {
    return WriterInfoResponse.of(
        writerInfo.getWriterId(),
        writerInfo.getWriterName(),
        writerInfo.getWriterProfileImageUrl()
    );
  }

  private static PostDetailResponse.WriterInfoResponse toDetailWriterInfoResponse(WriterInfo writerInfo) {
    return PostDetailResponse.WriterInfoResponse.of(
        writerInfo.getWriterId(),
        writerInfo.getWriterName(),
        writerInfo.getWriterProfileImageUrl()
    );
  }

  private static PostStatsResponse toStatsResponse(PostStats stats, long displayViewCount) {
    if (stats == null) {
      return PostStatsResponse.from(0L, displayViewCount, 0L);
    }
    return PostStatsResponse.from(
        stats.getLikeCount(),
        displayViewCount,
        stats.getCommentCount()
    );
  }

  private static PostStatsResponse toStatsResponse(PostStats stats) {
    if (stats == null) {
      return PostStatsResponse.from(0L, 0L, 0L);
    }
    return PostStatsResponse.from(
        stats.getLikeCount(),
        stats.getViewCount(),
        stats.getCommentCount()
    );
  }

  private static String summarize(String content, int maxLength) {
    if (content == null) {
      return "";
    }
    String stripped = content.strip();
    return stripped.length() > maxLength
        ? stripped.substring(0, maxLength) + "..."
        : stripped;
  }
}
