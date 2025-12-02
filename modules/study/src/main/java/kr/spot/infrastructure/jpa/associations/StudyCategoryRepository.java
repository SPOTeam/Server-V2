package kr.spot.infrastructure.jpa.associations;

import kr.spot.domain.associations.StudyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long> {

}
