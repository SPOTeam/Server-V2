package kr.spot.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.Study;
import kr.spot.exception.GeneralException;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {

  default Study getStudyById(Long studyId) {
    return findById(studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_NOT_FOUND));
  }
}
