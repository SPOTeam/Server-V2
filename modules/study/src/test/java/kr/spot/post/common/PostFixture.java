package kr.spot.post.common;

import kr.spot.post.domain.Post;
import kr.spot.post.domain.vo.WriterInfo;
import kr.spot.post.presentation.command.dto.ManagePostRequest;

public class PostFixture {

  public static final Long ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final Long WRITER_ID = 200L;
  public static final String WRITER_NAME = "테스트유저";
  public static final String WRITER_PROFILE_IMAGE_URL = "https://example.com/profile.jpg";
  public static final String TITLE = "테스트 게시글 제목";
  public static final String CONTENT = "테스트 게시글 내용입니다.";
  public static final boolean IS_PRIVATE = false;

  public static WriterInfo writerInfo() {
    return WriterInfo.of(WRITER_ID, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);
  }

  public static WriterInfo writerInfo(Long writerId) {
    return WriterInfo.of(writerId, WRITER_NAME, WRITER_PROFILE_IMAGE_URL);
  }

  public static Post post() {
    return Post.of(ID, STUDY_ID, writerInfo(), TITLE, CONTENT, IS_PRIVATE);
  }

  public static Post post(Long id, Long studyId) {
    return Post.of(id, studyId, writerInfo(), TITLE, CONTENT, IS_PRIVATE);
  }

  public static Post post(Long id, Long studyId, Long writerId) {
    return Post.of(id, studyId, writerInfo(writerId), TITLE, CONTENT, IS_PRIVATE);
  }

  public static ManagePostRequest managePostRequest() {
    return new ManagePostRequest(TITLE, CONTENT, IS_PRIVATE);
  }

  public static ManagePostRequest managePostRequest(String title, String content,
      boolean isPrivate) {
    return new ManagePostRequest(title, content, isPrivate);
  }
}
