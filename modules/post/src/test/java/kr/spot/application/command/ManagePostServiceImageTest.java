package kr.spot.application.command;

import static kr.spot.common.PostFixture.CONTENT;
import static kr.spot.common.PostFixture.POST_ID;
import static kr.spot.common.PostFixture.TITLE;
import static kr.spot.common.PostFixture.WRITER_ID;
import static kr.spot.common.PostFixture.writerInfo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.IdGenerator;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.domain.enums.PostType;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.infrastructure.jpa.PostImageRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.presentation.command.dto.request.ManagePostRequest;
import kr.spot.presentation.command.dto.response.CreatePostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ManagePostServiceImageTest {

    @Mock
    IdGenerator idGenerator;

    @Mock
    GetWriterInfoPort getWriterInfoPort;

    @Mock
    FileStoragePort fileStoragePort;

    @Mock
    PostRepository postRepository;

    @Mock
    PostStatsRepository postStatsRepository;

    @Mock
    PostImageRepository postImageRepository;

    @Captor
    ArgumentCaptor<Post> postCaptor;

    @Captor
    ArgumentCaptor<PostStats> postStatsCaptor;

    @Captor
    ArgumentCaptor<PostImage> postImageCaptor;

    ManagePostService managePostService;

    @BeforeEach
    void setUp() {
        managePostService = new ManagePostService(idGenerator, getWriterInfoPort, fileStoragePort, postRepository,
                postStatsRepository, postImageRepository);
    }

    @Test
    @DisplayName("이미지 파일과 함께 게시글을 정상적으로 생성할 수 있다.")
    void should_create_post_with_image_successfully() {
        // given
        ManagePostRequest request = new ManagePostRequest(TITLE, CONTENT, PostType.FREE_TALK);
        MultipartFile imageFile = new MockMultipartFile("image", "image.jpg", "image/jpeg",
                "test image content".getBytes());
        WriterInfo writerInfo = writerInfo();
        WriterInfoResponse writerInfoResponse = new WriterInfoResponse(WRITER_ID, writerInfo.getWriterName(),
                writerInfo.getWriterProfileImageUrl());
        UploadResult uploadResult = new UploadResult("http://example.com/image.jpg", "image.jpg");

        when(idGenerator.nextId()).thenReturn(POST_ID);
        when(getWriterInfoPort.get(WRITER_ID)).thenReturn(writerInfoResponse);
        when(fileStoragePort.upload(any(MultipartFile.class), anyString())).thenReturn(uploadResult);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postStatsRepository.save(any(PostStats.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(postImageRepository.save(any(PostImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CreatePostResponse response = managePostService.createPost(request, WRITER_ID, imageFile);

        // then
        assertThat(response).isNotNull();
        assertThat(response.postId()).isEqualTo(POST_ID);

        verify(getWriterInfoPort).get(WRITER_ID);
        verify(fileStoragePort).upload(imageFile, "posts/images/");
        verify(postRepository).save(postCaptor.capture());
        verify(postStatsRepository).save(postStatsCaptor.capture());
        verify(postImageRepository).save(postImageCaptor.capture());

        Post capturedPost = postCaptor.getValue();
        assertThat(capturedPost.getId()).isEqualTo(POST_ID);
        assertThat(capturedPost.getTitle()).isEqualTo(TITLE);
        assertThat(capturedPost.getContent()).isEqualTo(CONTENT);
        assertThat(capturedPost.getPostType()).isEqualTo(PostType.FREE_TALK);
        assertThat(capturedPost.getWriterInfo().getWriterId()).isEqualTo(WRITER_ID);

        PostStats capturedPostStats = postStatsCaptor.getValue();
        assertThat(capturedPostStats.getPostId()).isEqualTo(POST_ID);

        PostImage capturedPostImage = postImageCaptor.getValue();
        assertThat(capturedPostImage.getPostId()).isEqualTo(POST_ID);
        assertThat(capturedPostImage.getImageUrl()).isEqualTo(uploadResult.url());
    }

    @Test
    @DisplayName("이미지 파일과 함께 게시글을 정상적으로 수정할 수 있다.")
    void should_update_post_with_image_successfully() {
        // given
        ManagePostRequest request = new ManagePostRequest("Updated Title", "Updated Content", PostType.FREE_TALK);
        MultipartFile imageFile = new MockMultipartFile("image", "new_image.png", "image/png",
                "new image content".getBytes());
        UploadResult uploadResult = new UploadResult("http://example.com/new_image.png", "file_name");

        when(postRepository.updatePost(anyLong(), anyString(), anyString(), any(PostType.class), anyLong())).thenReturn(
                1);
        when(fileStoragePort.upload(any(MultipartFile.class), anyString())).thenReturn(uploadResult);
        when(postImageRepository.save(any(PostImage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        managePostService.updatePost(POST_ID, request, WRITER_ID, imageFile);

        // then
        verify(postRepository).updatePost(POST_ID, request.title(), request.content(), request.postType(), WRITER_ID);
        verify(fileStoragePort).upload(imageFile, "posts/images/");
        verify(postImageRepository).deleteByPostId(POST_ID);
        verify(postImageRepository).save(postImageCaptor.capture());

        PostImage capturedPostImage = postImageCaptor.getValue();
        assertThat(capturedPostImage.getPostId()).isEqualTo(POST_ID);
        assertThat(capturedPostImage.getImageUrl()).isEqualTo(uploadResult.url());
    }
}
