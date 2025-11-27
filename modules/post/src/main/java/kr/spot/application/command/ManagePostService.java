package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Post;
import kr.spot.domain.PostStats;
import kr.spot.domain.association.PostImage;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.exception.GeneralException;
import kr.spot.infrastructure.jpa.PostImageRepository;
import kr.spot.infrastructure.jpa.PostRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.presentation.command.dto.request.ManagePostRequest;
import kr.spot.presentation.command.dto.response.CreatePostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ManagePostService {

    private static final String FILE_DIR = "posts/images/";

    private final IdGenerator idGenerator;
    private final GetWriterInfoPort getWriterInfoPort;
    private final FileStoragePort fileStoragePort;

    private final PostRepository postRepository;
    private final PostStatsRepository postStatsRepository;
    private final PostImageRepository postImageRepository;

    public CreatePostResponse createPost(ManagePostRequest request, Long writerId, MultipartFile imageFile) {
        WriterInfo writerInfo = getWriterInfo(writerId);
        Post post = createAndSavePost(request, writerInfo);
        initializeAndSavePostStats(post);
        updatePostImage(post.getId(), imageFile);
        return CreatePostResponse.from(post.getId());
    }

    public void updatePost(Long postId, ManagePostRequest request, Long writerId, MultipartFile imageFiles) {
        int updated = postRepository.updatePost(postId, request.title(), request.content(), request.postType(),
                writerId);
        if (updated <= 0) {
            throw new GeneralException(ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
        }
        updatePostImage(postId, imageFiles);
    }

    public void deletePost(Long postId, Long writerId) {
        int deleted = postRepository.deletePost(postId, writerId);
        if (deleted <= 0) {
            throw new GeneralException(ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);
        }
    }

    private WriterInfo getWriterInfo(Long writerId) {
        WriterInfoResponse writerInfoResponse = getWriterInfoPort.get(writerId);
        return WriterInfo.of(writerInfoResponse.writerId(), writerInfoResponse.nickname(),
                writerInfoResponse.profileImageUrl());
    }

    private Post createAndSavePost(ManagePostRequest request, WriterInfo writerInfo) {
        Post post = Post.of(idGenerator.nextId(), writerInfo, request.title(), request.content(), request.postType());
        postRepository.save(post);
        return post;
    }

    private void updatePostImage(Long postId, MultipartFile imageFiles) {
        UploadResult upload = fileStoragePort.upload(imageFiles, FILE_DIR);
        postImageRepository.deleteByPostId(postId);
        PostImage postImage = PostImage.of(postId, upload.url());
        postImageRepository.save(postImage);
    }

    private void initializeAndSavePostStats(Post post) {
        PostStats postStats = PostStats.of(post.getId());
        postStatsRepository.save(postStats);
    }

}
