package kr.spot.study.infrastructure.jpa.associations;

import java.util.List;
import kr.spot.study.domain.associations.StudyCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyCategoryRepository extends JpaRepository<StudyCategory, Long> {

  List<StudyCategory> findAllByStudyId(long studyId);
}
