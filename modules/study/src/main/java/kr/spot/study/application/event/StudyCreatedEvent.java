package kr.spot.study.application.event;

import org.springframework.web.multipart.MultipartFile;

public record StudyCreatedEvent(
    Long studyId,
    MultipartFile imageFile
) {

  public static StudyCreatedEvent of(Long studyId, MultipartFile imageFile) {
    return new StudyCreatedEvent(studyId, imageFile);
  }

  public boolean hasImage() {
    return imageFile != null && !imageFile.isEmpty();
  }
}
