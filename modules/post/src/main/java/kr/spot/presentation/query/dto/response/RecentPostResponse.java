package kr.spot.presentation.query.dto.response;

import java.util.List;
import kr.spot.domain.enums.PostType;

public record RecentPostResponse(
    List<RecentPost> recentPosts
) {

  public static RecentPostResponse of(List<RecentPost> recentPosts) {
    return new RecentPostResponse(recentPosts);
  }

  public record RecentPost(
      Long postId,
      String title,
      long commentCount,
      PostType postType
  ) {

    public static RecentPost of(Long postId, String title, long commentCount, PostType postType) {
      return new RecentPost(postId, title, commentCount, postType);
    }
  }
}
