package kr.spot.post.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "studyCommentRepository")
public interface CommentRepository extends JpaRepository<Comment, Long> {

  default Comment getById(long id) {
    return findById(id)
        .orElseThrow(() -> new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));
  }

  void deleteByWriterInfoWriterId(long writerId);
}
