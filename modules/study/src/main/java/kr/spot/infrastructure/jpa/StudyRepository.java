package kr.spot.infrastructure.jpa;

import kr.spot.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {
  
}
