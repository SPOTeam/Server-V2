package kr.spot.study.infrastructure.jpa.associations;

import kr.spot.study.domain.associations.StudyStyle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyStyleRepository extends JpaRepository<StudyStyle, Long> {

}
