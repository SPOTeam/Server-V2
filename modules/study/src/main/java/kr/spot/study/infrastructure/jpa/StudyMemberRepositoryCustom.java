package kr.spot.study.infrastructure.jpa;

import java.util.List;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;

public interface StudyMemberRepositoryCustom {

  List<StudyApplicationInfo> findMyAppliedStudiesWithStudyInfo(
      Long memberId,
      StudyMemberStatus status
  );

  List<StudyMember> findApplicationsByStudyIdAndStatus(Long studyId, StudyMemberStatus status);
}
