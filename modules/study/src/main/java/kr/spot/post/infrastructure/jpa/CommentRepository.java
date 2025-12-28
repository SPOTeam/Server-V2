package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "studyCommentRepository")
public interface CommentRepository extends JpaRepository<Comment, Long> {

}
