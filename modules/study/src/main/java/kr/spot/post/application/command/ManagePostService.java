package kr.spot.post.application.command;

import kr.spot.IdGenerator;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.PostStats;
import kr.spot.post.domain.vo.WriterInfo;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.post.presentation.command.dto.ManagePostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManagePostService {

  private final IdGenerator idGenerator;
  private final GetWriterInfoPort getWriterInfoPort;
  private final PostRepository postRepository;
  private final PostStatsRepository postStatsRepository;

  public void createPost(long studyId, long writerId, ManagePostRequest request) {
    WriterInfo writerInfo = getWriterInfo(writerId);
    Post post = createAndSavePost(request, studyId, writerInfo);
    initializeAndSavePostStats(post);
  }

  public void updatePost(long studyId, long postId, ManagePostRequest request, long writerId) {
    Post post = postRepository.getById(postId);
    post.update(request.title(), request.content(), request.isPrivate(), writerId, studyId);
  }

  public void deletePost(long studyId, long postId, long writerId) {
    Post post = postRepository.getById(postId);
    post.delete(writerId, studyId);
  }

  public void pinPost(long studyId, long postId) {
    Post post = postRepository.getById(postId);

  }

  private WriterInfo getWriterInfo(long writerId) {
    WriterInfoResponse writerInfoResponse = getWriterInfoPort.get(writerId);
    return WriterInfo.of(writerInfoResponse.writerId(), writerInfoResponse.nickname(),
        writerInfoResponse.profileImageUrl());
  }

  private Post createAndSavePost(ManagePostRequest request, long studyId, WriterInfo writerInfo) {
    Post post = Post.of(idGenerator.nextId(), studyId, writerInfo, request.title(),
        request.content(),
        request.isPrivate());
    postRepository.save(post);
    return post;
  }

  private void initializeAndSavePostStats(Post post) {
    PostStats postStats = PostStats.of(post.getId());
    postStatsRepository.save(postStats);
  }
}
