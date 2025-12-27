package kr.spot.study.infrastructure.jpa.associations;

import kr.spot.study.domain.associations.StudyStats;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyStatsRepository extends JpaRepository<StudyStats, Long> {

}
