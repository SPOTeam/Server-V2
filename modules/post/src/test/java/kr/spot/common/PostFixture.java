package kr.spot.common;

import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.domain.enums.PostType;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.presentation.command.dto.request.ManagePostRequest;
import org.springframework.test.util.ReflectionTestUtils;

public class PostFixture {

  public static Long POST_ID = 1L;
  public static String TITLE = "제목";
  public static String CONTENT = "내용";
  public static String IMAGE_URL1 = "https://example.com/image1.jpg";
  public static String IMAGE_URL2 = "https://example.com/image2.jpg";

  public static String UPDATED_TITLE = "수정된 제목";
  public static String UPDATED_CONTENT = "수정된 내용";
  public static String UPDATED_IMAGE_URL = "https://example.com/updated_image.jpg";

  public static Long WRITER_ID = 1L;
  public static String WRITER_NAME = "작성자";
  public static String WRITER_IMAGE_URL = "https://example.com/image.jpg";
  public static Long OTHER_WRITER_ID = 2L;


  public static Post post() {
    return Post.of(POST_ID, writerInfo(), TITLE, CONTENT, PostType.COUNSELING);
  }

  public static Post post(Long postId) {
    return Post.of(postId, writerInfo(), TITLE, CONTENT, PostType.COUNSELING);
  }

  public static Post post(Long postId, PostType postType) {
    return Post.of(postId, writerInfo(), TITLE, CONTENT, postType);
  }

  public static PostStats postStats() {
    return PostStats.of(POST_ID);
  }

  public static PostImage postImage() {
    return PostImage.of(POST_ID, IMAGE_URL1);
  }

  public static PostStats postStats(Long postId, long count) {
    PostStats postStats = PostStats.of(postId);
    ReflectionTestUtils.setField(postStats, "commentCount", count);
    return postStats;
  }

  public static WriterInfo writerInfo() {
    return WriterInfo.of(WRITER_ID, WRITER_NAME, WRITER_IMAGE_URL);
  }

  public static ManagePostRequest updatePostRequest() {
    return new ManagePostRequest(UPDATED_TITLE, UPDATED_CONTENT, PostType.COUNSELING);
  }
}
