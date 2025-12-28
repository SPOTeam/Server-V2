package kr.spot.post.infrastructure.jpa;

import kr.spot.post.domain.PostStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository(value = "studyPostStatsRepository")
public interface PostStatsRepository extends JpaRepository<PostStats, Long> {

}
