package kr.spot.study.infrastructure.jpa;

import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {

  default Study getStudyById(Long studyId) {
    return findById(studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._STUDY_NOT_FOUND));
  }
}
