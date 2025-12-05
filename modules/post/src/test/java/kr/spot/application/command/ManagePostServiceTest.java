//package kr.spot.application.command;
//
//import static kr.spot.common.PostFixture.CONTENT;
//import static kr.spot.common.PostFixture.OTHER_WRITER_ID;
//import static kr.spot.common.PostFixture.POST_ID;
//import static kr.spot.common.PostFixture.TITLE;
//import static kr.spot.common.PostFixture.WRITER_ID;
//import static kr.spot.common.PostFixture.writerInfo;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.anyLong;
//import static org.mockito.Mockito.when;
//
//import kr.spot.IdGenerator;
//import kr.spot.common.PostFixture;
//import kr.spot.domain.Post;
//import kr.spot.domain.enums.PostType;
//import kr.spot.domain.vo.WriterInfo;
//import kr.spot.exception.GeneralException;
//import kr.spot.infrastructure.jpa.PostImageRepository;
//import kr.spot.infrastructure.jpa.PostLikeRepository;
//import kr.spot.infrastructure.jpa.PostRepository;
//import kr.spot.infrastructure.jpa.PostStatsRepository;
//import kr.spot.ports.FileStoragePort;
//import kr.spot.ports.GetWriterInfoPort;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Disabled;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//
//@ExtendWith(MockitoExtension.class)
//class ManagePostServiceTest {
//
//  @Mock
//  IdGenerator idGenerator;
//
//  @Mock
//  GetWriterInfoPort getWriterInfoPort;
//
//  @Mock
//  FileStoragePort fileStoragePort;
//
//  @Mock
//  PostRepository postRepository;
//
//  @Mock
//  PostStatsRepository postStatsRepository;
//
//  @Mock
//  PostLikeRepository postLikeRepository;
//
//  @Mock
//  PostImageRepository postImageRepository;
//
//  ManagePostService managePostService;
//  LikePostService likePostService;
//
//  @BeforeEach
//  void setUp() {
//    managePostService = new ManagePostService(idGenerator, getWriterInfoPort, fileStoragePort,
//        postRepository,
//        postStatsRepository, postImageRepository);
//    likePostService = new LikePostService(idGenerator, postLikeRepository, postStatsRepository);
//  }
//
//  @Test
//  @DisplayName("게시글을 정상적으로 생성할 수 있다.")
//  void should_create_post_successfully() {
//    // given
//    WriterInfo writerInfo = writerInfo();
//
//    // when
//    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);
//
//    // then
//    assertThat(post).isNotNull();
//    assertThat(post.getId()).isEqualTo(POST_ID);
//    assertThat(post.getTitle()).isEqualTo(TITLE);
//    assertThat(post.getContent()).isEqualTo(CONTENT);
//    assertThat(post.getPostType()).isEqualTo(PostType.FREE_TALK);
//  }
//
//  @Test
//  @Disabled
//  @DisplayName("게시글 작성자가 아닌 경우 게시글 수정에 실패한다.")
//  void should_fail_to_update_post_when_not_writer() {
//    // given
//    WriterInfo writerInfo = writerInfo();
//    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);
//
//    when(postRepository.getPostByIdWithLock(POST_ID)).thenReturn(post);
//
//    // when & then
//    assertThatThrownBy(() ->
//        managePostService.updatePost(POST_ID, PostFixture.updatePostRequest(), OTHER_WRITER_ID,
//            null)
//    ).isInstanceOf(GeneralException.class);
//  }
//
//  @Test
//  @Disabled
//  @DisplayName("게시글 작성자가 아닌 경우 게시글 삭제에 실패한다.")
//  void should_fail_to_delete_post_when_not_writer() {
//    // given
//    WriterInfo writerInfo = writerInfo();
//    Post post = Post.of(POST_ID, writerInfo, TITLE, CONTENT, PostType.FREE_TALK);
//
//    when(postRepository.getPostByIdWithLock(POST_ID)).thenReturn(post);
//
//    // when & then
//    assertThatThrownBy(() ->
//        managePostService.deletePost(POST_ID, OTHER_WRITER_ID)
//    ).isInstanceOf(GeneralException.class);
//  }
//
//  @Test
//  @DisplayName("이미 좋아요를 누른 게시글에 대해 다시 좋아요를 누를 경우 정상적으로 처리된다.")
//  void should_process_successfully_when_liking_already_liked_post() {
//    // given
//    when(postLikeRepository.savePostLike(anyLong(), anyLong(), anyLong()))
//        .thenReturn(0);
//
//    // when & then
//    likePostService.likePost(POST_ID, WRITER_ID);
//  }
//
//
//  @Test
//  @DisplayName("좋아요를 누르지 않은 게시글에 대해 좋아요 취소를 할 경우 정상적으로 처리된다.")
//  void should_process_successfully_when_unliking_not_liked_post() {
//    // given
//    when(postLikeRepository.hardDelete(POST_ID, WRITER_ID)).thenReturn(0);
//
//    // when & then
//    likePostService.unlikePost(POST_ID, WRITER_ID);
//  }
//}
