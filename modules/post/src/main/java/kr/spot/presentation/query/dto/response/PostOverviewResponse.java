package kr.spot.presentation.query.dto.response;

import java.util.List;
import kr.spot.domain.enums.PostType;

public record PostOverviewResponse(
    List<PostOverview> hotPosts
) {

  public static kr.spot.presentation.query.dto.response.PostOverviewResponse of(
      List<PostOverview> hotPosts) {
    return new kr.spot.presentation.query.dto.response.PostOverviewResponse(hotPosts);
  }

  public record PostOverview(
      Long postId,
      String title,
      String content,
      Long commentCount,
      PostType postType
  ) {

    public static PostOverview of(Long postId, String title, String content,
        Long commentCount, PostType postType) {
      return new PostOverview(postId, title, content, commentCount, postType);
    }
  }
}
