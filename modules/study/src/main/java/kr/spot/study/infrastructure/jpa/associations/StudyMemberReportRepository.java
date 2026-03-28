package kr.spot.study.infrastructure.jpa.associations;

import kr.spot.study.domain.associations.StudyMemberReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberReportRepository extends JpaRepository<StudyMemberReport, Long> {

}
