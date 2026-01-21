package kr.spot.study.application.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import kr.spot.study.domain.Study;
import kr.spot.study.presentation.query.dto.response.GetStudyOverviewResponse;
import kr.spot.study.presentation.query.dto.response.GetStudyOverviewResponse.StudyOverview;

public class StudyDTOMapper {

  public static GetStudyOverviewResponse toDTO(
      List<Study> studies,
      Set<Long> likedStudyIds,
      Set<Long> ownedStudyIds,
      boolean hasNext,
      Long nextCursor,
      Long totalElements
  ) {
    Set<Long> safelikedIds = likedStudyIds != null ? likedStudyIds : Collections.emptySet();
    Set<Long> safeOwnedStudyIds = ownedStudyIds != null ? ownedStudyIds : Collections.emptySet();

    List<StudyOverview> list = studies.stream().map(
        study -> StudyOverview.of(
            study.getId(),
            study.getName(),
            study.getDescription(),
            study.getMaxMembers(),
            study.getCurrentMembers(),
            0,
            safelikedIds.contains(study.getId()),
            safeOwnedStudyIds.contains(study.getId()),
            0,
            study.getImageUrl()
        )
    ).toList();

    return GetStudyOverviewResponse.of(list, hasNext, nextCursor, totalElements);
  }
}
