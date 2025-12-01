package kr.spot.presentation.query.dto.response;

import java.util.List;

public record GetMyAppliedStudyResponse(
        List<MyAppliedStudy> studies
) {

    public static GetMyAppliedStudyResponse of(List<MyAppliedStudy> studies) {
        return new GetMyAppliedStudyResponse(studies);
    }

    public record MyAppliedStudy(
            Long studyId,
            String title,
            String profileImageUrl
    ) {
        public static MyAppliedStudy of(Long studyId, String title, String profileImageUrl) {
            return new MyAppliedStudy(studyId, title, profileImageUrl);
        }
    }
}
