package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.domain.Comment;
import kr.spot.domain.vo.WriterInfo;
import kr.spot.infrastructure.jpa.CommentRepository;
import kr.spot.infrastructure.jpa.PostStatsRepository;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.presentation.command.dto.request.ManageCommentRequest;
import kr.spot.presentation.command.dto.response.CreateCommentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageCommentService {

  private final IdGenerator idGenerator;
  private final GetWriterInfoPort getWriterInfoPort;
  private final CommentRepository commentRepository;
  private final PostStatsRepository postStatsRepository;

  public CreateCommentResponse createComment(Long writerId, Long postId,
      ManageCommentRequest request) {
    WriterInfo writerInfo = getWriterInfo(writerId);
    Comment comment = Comment.of(idGenerator.nextId(), postId, writerInfo, request.content());
    Comment save = saveCommentAndIncreaseCommentCount(postId, comment);
    return CreateCommentResponse.of(save.getId());
  }

  public void updateComment(Long writerId, Long commentId, ManageCommentRequest request) {
    Comment comment = commentRepository.getById(commentId);
    comment.update(writerId, request.content());
  }

  public void deleteComment(Long writerId, Long commentId) {
    Comment comment = commentRepository.getById(commentId);
    comment.delete(writerId);
    postStatsRepository.decreaseCommentCount(comment.getPostId());
  }

  private WriterInfo getWriterInfo(Long writerId) {
    WriterInfoResponse writerInfoResponse = getWriterInfoPort.get(writerId);
    return WriterInfo.of(writerInfoResponse.writerId(), writerInfoResponse.nickname(),
        writerInfoResponse.profileImageUrl());
  }

  private Comment saveCommentAndIncreaseCommentCount(Long postId, Comment comment) {
    Comment save = commentRepository.save(comment);
    postStatsRepository.increaseCommentCount(postId);
    return save;
  }
}
