package kr.spot.infrastructure.jpa;

import java.util.Optional;
import kr.spot.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

  Optional<RefreshToken> findByMemberId(Long memberId);

  void deleteByMemberId(Long memberId);
}
