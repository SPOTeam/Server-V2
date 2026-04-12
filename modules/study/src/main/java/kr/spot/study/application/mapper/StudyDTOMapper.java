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
      Set<Long> aloneStudyIds,
      boolean hasNext,
      Long nextCursor,
      Long totalElements
  ) {
    Set<Long> safeLikedIds = likedStudyIds != null ? likedStudyIds : Collections.emptySet();
    Set<Long> safeOwnedStudyIds = ownedStudyIds != null ? ownedStudyIds : Collections.emptySet();
    Set<Long> safeAloneStudyIds = aloneStudyIds != null ? aloneStudyIds : Collections.emptySet();

    List<StudyOverview> list = studies.stream().map(
        study -> StudyOverview.of(
            study.getId(),
            study.getName(),
            study.getDescription(),
            study.getMaxMembers(),
            study.getCurrentMembers(),
            study.getLikeCount(),
            safeLikedIds.contains(study.getId()),
            safeOwnedStudyIds.contains(study.getId()),
            safeAloneStudyIds.contains(study.getId()),
            study.getViewCount(),
            study.getImageUrl()
        )
    ).toList();

    return GetStudyOverviewResponse.of(list, hasNext, nextCursor, totalElements);
  }
}
