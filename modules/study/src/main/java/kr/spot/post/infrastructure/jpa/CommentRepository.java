package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
