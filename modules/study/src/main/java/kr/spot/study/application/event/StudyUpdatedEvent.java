package kr.spot.study.application.event;

import org.springframework.web.multipart.MultipartFile;

public record StudyUpdatedEvent(
    Long studyId,
    MultipartFile imageFile
) {

  public static StudyUpdatedEvent of(Long studyId, MultipartFile imageFile) {
    return new StudyUpdatedEvent(studyId, imageFile);
  }

  public boolean hasImage() {
    return imageFile != null && !imageFile.isEmpty();
  }
}
