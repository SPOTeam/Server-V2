package kr.spot.application.query.mapper;

import java.util.List;
import kr.spot.domain.Comment;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.presentation.query.dto.response.PostDetailResponse;
import kr.spot.presentation.query.dto.response.PostListResponse.PostList;
import kr.spot.presentation.query.dto.response.PostOverviewResponse.PostOverview;
import kr.spot.presentation.query.dto.response.PostStatsResponse;
import kr.spot.presentation.query.dto.response.RecentPostResponse.RecentPost;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class PostResponseMapper {

  private static final int MAX_CONTENT_LENGTH = 100;

  /* -------- 공통 -------- */
  private static String summarize(String content) {
    if (content == null) {
      return "";
    }
    content = content.strip();
    return content.length() > MAX_CONTENT_LENGTH
        ? content.substring(0, MAX_CONTENT_LENGTH) + "..."
        : content;
  }

  /* -------- 목록용 -------- */
  public static PostList toPostList(Post p, PostStats st, boolean liked) {
    long view = st.getViewCount();
    long like = st.getLikeCount();
    long comment = st.getCommentCount();

    return PostList.of(
        p.getId(),
        p.getTitle(),
        summarize(p.getContent()),
        new PostStatsResponse(like, view, comment),
        p.getCreatedAt(),
        liked
    );
  }

  /* -------- 상세용 -------- */
  public static PostDetailResponse toPostDetail(Post post, PostStats st, PostImage postImage,
      long displayView,
      List<Comment> comments) {
    return PostDetailResponse.builder()
        .postId(post.getId())
        .title(post.getTitle())
        .content(post.getContent())
        .imageUrl(postImage.getImageUrl())
        .postType(post.getPostType())
        .writer(PostDetailResponse.WriterInfoResponse.of(
            post.getWriterInfo().getWriterId(),
            post.getWriterInfo().getWriterName(),
            post.getWriterInfo().getWriterProfileImageUrl()
        ))
        .stats(PostStatsResponse.from(
            st.getLikeCount(), displayView, st.getCommentCount()))
        .createdAt(post.getCreatedAt())
        .comments(
            comments.stream()
                .map(c -> PostDetailResponse.CommentResponse.of(
                    c.getId(),
                    c.getContent(),
                    PostDetailResponse.WriterInfoResponse.of(
                        c.getWriterInfo().getWriterId(),
                        c.getWriterInfo().getWriterName(),
                        c.getWriterInfo().getWriterProfileImageUrl()
                    ),
                    c.getCreatedAt()
                ))
                .toList()
        )
        .commentCount(comments.size())
        .build();
  }

  /* -------- 인기/오버뷰 -------- */
  public static PostOverview toPostOverview(Post p, PostStats st) {
    return PostOverview.of(
        p.getId(),
        p.getTitle(),
        summarize(p.getContent()),
        st.getCommentCount(),
        p.getPostType()
    );
  }

  /* -------- 카테고리별 최신 -------- */
  public static RecentPost toRecentPost(Post p, PostStats st) {
    return RecentPost.of(
        p.getId(),
        p.getTitle(),
        st.getCommentCount(),
        p.getPostType()
    );
  }
}