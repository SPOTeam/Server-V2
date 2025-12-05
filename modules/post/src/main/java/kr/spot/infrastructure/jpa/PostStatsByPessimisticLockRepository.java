package kr.spot.infrastructure.jpa;


import jakarta.persistence.LockModeType;
import java.util.Optional;
import kr.spot.domain.PostStatsByPessimisticLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface PostStatsByPessimisticLockRepository extends
    JpaRepository<PostStatsByPessimisticLock, Long> {

  // 비관 락
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<PostStatsByPessimisticLock> findByPostId(Long postId);
}
