package kr.spot.infrastructure.jpa;

import java.util.List;
import kr.spot.domain.enums.StudyMemberStatus;
import kr.spot.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;

public interface StudyMemberRepositoryCustom {

  List<StudyApplicationInfo> findMyAppliedStudiesWithStudyInfo(
      Long memberId,
      StudyMemberStatus status
  );
}
