package kr.spot.study.application.event;

import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.study.domain.Study;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class StudyImageUploadListener {

  private static final String FILE_DIR = "studies/images/";

  private final FileStoragePort fileStoragePort;
  private final StudyRepository studyRepository;

  @Async("imageUploadExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleStudyCreated(StudyCreatedEvent event) {
    if (!event.hasImage()) {
      return;
    }
    uploadAndApply(event.studyId(), event.imageFile());
  }

  @Async("imageUploadExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleStudyUpdated(StudyUpdatedEvent event) {
    if (!event.hasImage()) {
      return;
    }
    uploadAndApply(event.studyId(), event.imageFile());
  }

  private void uploadAndApply(Long studyId, MultipartFile file) {
    try {
      UploadResult result = fileStoragePort.upload(file, FILE_DIR);
      Study study = studyRepository.getStudyById(studyId);
      study.updateImageUrl(result.url());
      log.info("Successfully uploaded image for study: {}", studyId);
    } catch (Exception e) {
      log.error("Failed to upload image for study: {}", studyId, e);
    }
  }
}
