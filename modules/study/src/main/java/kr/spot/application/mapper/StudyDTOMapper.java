package kr.spot.application.mapper;

import java.util.List;
import kr.spot.domain.Study;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse;
import kr.spot.presentation.query.dto.response.GetStudyOverviewResponse.StudyOverview;

public class StudyDTOMapper {

  public static GetStudyOverviewResponse toDTO(List<Study> studies, boolean hasNext,
      Long nextCursor,
      Long totalElements) {
    List<StudyOverview> list = studies.stream().map(
        study -> StudyOverview.of(
            study.getId(),
            study.getName(),
            study.getDescription(),
            study.getMaxMembers(),
            study.getCurrentMembers(),
            0,
            false,
            0,
            study.getImageUrl()
        )
    ).toList();

    return GetStudyOverviewResponse.of(list, hasNext, nextCursor, totalElements);
  }
}
