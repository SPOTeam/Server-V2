package kr.spot.study.infrastructure.jpa.associations;

import java.util.List;
import kr.spot.study.domain.associations.StudyRegion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRegionRepository extends JpaRepository<StudyRegion, Long> {

  List<StudyRegion> findAllByStudyId(long studyId);
}
