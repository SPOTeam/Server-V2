package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.PostStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostStatsRepository extends JpaRepository<PostStats, Long> {

}
