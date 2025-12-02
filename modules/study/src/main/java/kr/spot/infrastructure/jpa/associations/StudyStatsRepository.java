package kr.spot.infrastructure.jpa.associations;

import kr.spot.domain.associations.StudyStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyStatsRepository extends JpaRepository<StudyStats, Long> {
  
}
