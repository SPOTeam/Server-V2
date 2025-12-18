package kr.spot.infrastructure.jpa;

import java.util.Optional;
import kr.spot.domain.association.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

  void deleteByPostId(Long postId);

  Optional<PostImage> findByPostId(Long postId);

  default PostImage getPostImageByPostId(Long id) {
    return findByPostId(id).orElse(null);
  }
}
