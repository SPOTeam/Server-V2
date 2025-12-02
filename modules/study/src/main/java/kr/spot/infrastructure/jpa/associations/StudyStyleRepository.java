package kr.spot.infrastructure.jpa.associations;

import kr.spot.domain.associations.StudyStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyStyleRepository extends JpaRepository<StudyStyle, Long> {
  
}
