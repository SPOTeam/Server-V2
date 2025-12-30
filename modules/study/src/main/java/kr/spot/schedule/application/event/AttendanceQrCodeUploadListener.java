package kr.spot.schedule.application.event;

import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.schedule.domain.Schedule;
import kr.spot.schedule.infrastructure.jpa.ScheduleRepository;
import kr.spot.schedule.infrastructure.qr.ByteArrayMultipartFile;
import kr.spot.schedule.infrastructure.qr.QrCodeGenerator;
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
public class AttendanceQrCodeUploadListener {

  private static final String FILE_DIR = "attendance/qr/";
  private static final String CONTENT_TYPE = "image/png";

  private final QrCodeGenerator qrCodeGenerator;
  private final FileStoragePort fileStoragePort;
  private final ScheduleRepository scheduleRepository;

  @Async("imageUploadExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handleAttendanceStarted(AttendanceStartedEvent event) {
    try {
      byte[] qrCodeImage = qrCodeGenerator.generate(event.qrContent());
      String filename = generateFilename(event.studyId(), event.scheduleId());
      MultipartFile multipartFile = new ByteArrayMultipartFile(qrCodeImage, filename, CONTENT_TYPE);

      UploadResult result = fileStoragePort.upload(multipartFile, FILE_DIR);

      Schedule schedule = scheduleRepository.getById(event.scheduleId());
      schedule.updateQrCodeImageUrl(result.url());

      log.info("Successfully uploaded QR code image for schedule: {}", event.scheduleId());
    } catch (Exception e) {
      log.error("Failed to upload QR code image for schedule: {}", event.scheduleId(), e);
    }
  }

  private String generateFilename(long studyId, long scheduleId) {
    return String.format("study_%d_schedule_%d_%d.png", studyId, scheduleId,
        System.currentTimeMillis());
  }
}
