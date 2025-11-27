package kr.spot.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.association.PostImage;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {

    void deleteByPostId(Long postId);

    default PostImage getPostImageById(Long id) {
        return findById(id).orElseThrow(() -> new GeneralException(ErrorStatus._POST_NOT_FOUND));
    }
}
