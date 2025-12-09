package kr.spot.common;

import java.util.List;
import java.util.stream.LongStream;
import kr.spot.infrastructure.jpa.querydsl.dto.StudyApplicationInfo;

public class StudyApplicationInfoFixture {

  public static final Long STUDY_MEMBER_ID = 1L;
  public static final Long STUDY_ID = 100L;
  public static final String STUDY_NAME = "스터디 이름";
  public static final String STUDY_PROFILE_IMAGE_URL = "http://example.com/study-image.png";

  public static StudyApplicationInfo create() {
    return new StudyApplicationInfo(STUDY_MEMBER_ID, STUDY_ID, STUDY_NAME, STUDY_PROFILE_IMAGE_URL);
  }

  public static StudyApplicationInfo create(Long studyMemberId, Long studyId) {
    return new StudyApplicationInfo(studyMemberId, studyId, STUDY_NAME + " " + studyId,
        STUDY_PROFILE_IMAGE_URL);
  }

  public static StudyApplicationInfo create(Long studyMemberId, Long studyId, String studyName,
      String studyProfileImageUrl) {
    return new StudyApplicationInfo(studyMemberId, studyId, studyName, studyProfileImageUrl);
  }

  public static List<StudyApplicationInfo> createList(int count) {
    return LongStream.rangeClosed(1, count)
        .mapToObj(i -> create(i, 100L + i))
        .toList();
  }
}