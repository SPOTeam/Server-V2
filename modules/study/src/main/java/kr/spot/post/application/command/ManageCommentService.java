package kr.spot.post.application.command;

import kr.spot.IdGenerator;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.post.domain.Comment;
import kr.spot.post.domain.Post;
import kr.spot.post.domain.vo.WriterInfo;
import kr.spot.post.infrastructure.jpa.CommentRepository;
import kr.spot.post.infrastructure.jpa.PostRepository;
import kr.spot.post.infrastructure.jpa.PostStatsRepository;
import kr.spot.post.presentation.command.dto.CreateCommentResponse;
import kr.spot.post.presentation.command.dto.ManageCommentRequest;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service(value = "ManageStudyCommentService")
@Transactional
@RequiredArgsConstructor
public class ManageCommentService {

  private final IdGenerator idGenerator;
  private final GetWriterInfoPort getWriterInfoPort;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final PostStatsRepository postStatsRepository;
  private final StudyAccessValidator studyAccessValidator;

  public CreateCommentResponse createComment(long writerId, long postId, long studyId,
      ManageCommentRequest request) {
    studyAccessValidator.validateStudyMember(studyId, writerId);
    validatePostBelongsToStudy(postId, studyId);
    WriterInfo writerInfo = getWriterInfo(writerId);
    Comment comment = Comment.of(idGenerator.nextId(), postId, writerInfo, request.content());
    Comment save = saveCommentAndIncreaseCommentCount(postId, comment);
    return CreateCommentResponse.of(save.getId());
  }

  public void updateComment(long writerId, long commentId, long studyId,
      ManageCommentRequest request) {
    studyAccessValidator.validateStudyMember(studyId, writerId);
    Comment comment = commentRepository.getById(commentId);
    validatePostBelongsToStudy(comment.getPostId(), studyId);
    comment.update(writerId, request.content());
  }

  public void deleteComment(long writerId, long commentId, long studyId) {
    studyAccessValidator.validateStudyMember(studyId, writerId);
    Comment comment = commentRepository.getById(commentId);
    validatePostBelongsToStudy(comment.getPostId(), studyId);
    comment.delete(writerId);
    postStatsRepository.decreaseCommentCount(comment.getPostId());
  }

  private void validatePostBelongsToStudy(long postId, long studyId) {
    Post post = postRepository.getById(postId);
    post.validateBelongsToStudy(studyId);
  }

  private WriterInfo getWriterInfo(long writerId) {
    WriterInfoResponse writerInfoResponse = getWriterInfoPort.get(writerId);
    return WriterInfo.of(writerInfoResponse.writerId(), writerInfoResponse.nickname(),
        writerInfoResponse.profileImageUrl());
  }

  private Comment saveCommentAndIncreaseCommentCount(long postId, Comment comment) {
    Comment save = commentRepository.save(comment);
    postStatsRepository.increaseCommentCount(postId);
    return save;
  }
}
