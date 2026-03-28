package kr.spot.infrastructure.jpa;

import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Comment;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Query("select c from Comment c where c.postId = :postId and c.status = 'ACTIVE' order by c.createdAt asc")
  List<Comment> getCommentsByPostId(@Param("postId") Long postId);

  default Comment getById(Long id) {
    return findById(id)
        .orElseThrow(() -> new GeneralException(ErrorStatus._COMMENT_NOT_FOUND));
  }

  void deleteByWriterInfoWriterId(long writerId);
}
