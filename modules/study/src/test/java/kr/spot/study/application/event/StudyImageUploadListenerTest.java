package kr.spot.study.application.event;

import static kr.spot.study.common.StudyFixture.DESCRIPTION;
import static kr.spot.study.common.StudyFixture.FEE_AMOUNT;
import static kr.spot.study.common.StudyFixture.HAS_FEE;
import static kr.spot.study.common.StudyFixture.LEADER_ID;
import static kr.spot.study.common.StudyFixture.MAX_MEMBERS;
import static kr.spot.study.common.StudyFixture.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class StudyImageUploadListenerTest {

  @Mock
  FileStoragePort fileStoragePort;

  @Mock
  StudyRepository studyRepository;

  StudyImageUploadListener listener;

  @BeforeEach
  void setUp() {
    listener = new StudyImageUploadListener(fileStoragePort, studyRepository);
  }

  @Nested
  @DisplayName("스터디 생성 이벤트 처리 (handleStudyCreated)")
  class HandleStudyCreated {

    @Test
    @DisplayName("이미지가 있으면 S3에 업로드하고 스터디 이미지 URL을 업데이트한다")
    void should_upload_image_and_update_study_when_image_exists() {
      // given
      Long studyId = 1L;
      MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
          "content".getBytes());
      StudyCreatedEvent event = StudyCreatedEvent.of(studyId, imageFile);

      String uploadedUrl = "http://s3.example.com/studies/images/test.jpg";
      UploadResult uploadResult = new UploadResult(uploadedUrl, "test.jpg");
      Study study = Study.of(studyId, LEADER_ID, NAME, MAX_MEMBERS, Fee.of(HAS_FEE, FEE_AMOUNT),
          DESCRIPTION);

      when(fileStoragePort.upload(any(MultipartFile.class), anyString())).thenReturn(uploadResult);
      when(studyRepository.getStudyById(studyId)).thenReturn(study);

      // when
      listener.handleStudyCreated(event);

      // then
      verify(fileStoragePort).upload(imageFile, "studies/images/");
      verify(studyRepository).getStudyById(studyId);
      assertThat(study.getImageUrl()).isEqualTo(uploadedUrl);
    }

    @Test
    @DisplayName("이미지가 없으면 업로드를 수행하지 않는다")
    void should_not_upload_when_no_image() {
      // given
      StudyCreatedEvent event = StudyCreatedEvent.of(1L, null);

      // when
      listener.handleStudyCreated(event);

      // then
      verify(fileStoragePort, never()).upload(any(), anyString());
      verify(studyRepository, never()).getStudyById(any());
    }

    @Test
    @DisplayName("빈 이미지 파일이면 업로드를 수행하지 않는다")
    void should_not_upload_when_image_is_empty() {
      // given
      MockMultipartFile emptyFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
      StudyCreatedEvent event = StudyCreatedEvent.of(1L, emptyFile);

      // when
      listener.handleStudyCreated(event);

      // then
      verify(fileStoragePort, never()).upload(any(), anyString());
      verify(studyRepository, never()).getStudyById(any());
    }

    @Test
    @DisplayName("업로드 실패 시 예외를 던지지 않고 로그만 남긴다")
    void should_not_throw_exception_when_upload_fails() {
      // given
      Long studyId = 1L;
      MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
          "content".getBytes());
      StudyCreatedEvent event = StudyCreatedEvent.of(studyId, imageFile);

      when(fileStoragePort.upload(any(MultipartFile.class), anyString()))
          .thenThrow(new RuntimeException("S3 upload failed"));

      // when & then (예외가 발생하지 않음)
      listener.handleStudyCreated(event);

      verify(fileStoragePort).upload(imageFile, "studies/images/");
    }
  }
}
