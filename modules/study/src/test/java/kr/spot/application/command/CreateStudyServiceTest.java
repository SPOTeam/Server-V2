package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import kr.spot.IdGenerator;
import kr.spot.domain.Study;
import kr.spot.domain.enums.Category;
import kr.spot.domain.enums.Style;
import kr.spot.infrastructure.jpa.StudyRepository;
import kr.spot.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.infrastructure.jpa.associations.StudyStatsRepository;
import kr.spot.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.presentation.command.dto.request.CreateStudyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CreateStudyServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  FileStoragePort fileStoragePort;

  @Mock
  StudyRepository studyRepository;

  @Mock
  StudyStyleRepository studyStyleRepository;

  @Mock
  StudyRegionRepository studyRegionRepository;

  @Mock
  StudyCategoryRepository studyCategoryRepository;

  @Mock
  StudyStatsRepository studyStatsRepository;

  @Mock
  StudyMemberRepository studyMemberRepository;

  @Captor
  ArgumentCaptor<Study> studyCaptor;

  CreateStudyService createStudyService;

  @BeforeEach
  void setUp() {
    createStudyService = new CreateStudyService(idGenerator, fileStoragePort, studyRepository,
        studyStyleRepository, studyRegionRepository, studyCategoryRepository, studyStatsRepository,
        studyMemberRepository);
  }

  @Test
  @DisplayName("이미지 파일과 함께 스터디를 정상적으로 생성할 수 있다.")
  void should_create_study_with_image_successfully() {
    // given
    Long leaderId = 1L;
    String studyName = "Test Study";
    Integer maxMembers = 10;
    Boolean hasFee = true;
    Integer amount = 10000;
    String description = "Test Description";
    Set<Category> categories = Set.of(Category.LANGUAGE);
    Set<Style> styles = Set.of(Style.DISCUSSION_BASED);
    Set<String> regionCodes = Set.of("SEOUL");

    CreateStudyRequest request = new CreateStudyRequest(
        studyName, maxMembers, hasFee, amount, description, categories, styles, regionCodes
    );
    MultipartFile imageFile = new MockMultipartFile("image", "study.jpg", "image/jpeg",
        "study image content".getBytes());
    UploadResult uploadResult = new UploadResult("http://example.com/study.jpg", "study.jpg");

    when(idGenerator.nextId()).thenReturn(100L, 101L, 102L,
        103L); // For study, category, style, region
    when(fileStoragePort.upload(any(MultipartFile.class), anyString())).thenReturn(uploadResult);
    when(studyRepository.save(any(Study.class))).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(studyCategoryRepository.saveAll(any())).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(studyStyleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(studyRegionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    createStudyService.createStudy(request, leaderId, imageFile);

    // then
    verify(fileStoragePort).upload(imageFile, "studies/images/");
    verify(studyRepository).save(studyCaptor.capture());
    verify(studyCategoryRepository).saveAll(any());
    verify(studyStyleRepository).saveAll(any());
    verify(studyRegionRepository).saveAll(any());

    Study capturedStudy = studyCaptor.getValue();
    assertThat(capturedStudy.getId()).isEqualTo(100L);
    assertThat(capturedStudy.getLeaderId()).isEqualTo(leaderId);
    assertThat(capturedStudy.getName()).isEqualTo(studyName);
    assertThat(capturedStudy.getImageUrl()).isEqualTo(uploadResult.url());
  }

  @Test
  @DisplayName("이미지 파일 없이 스터디를 정상적으로 생성할 수 있다.")
  void should_create_study_without_image_successfully() {
    // given
    Long leaderId = 1L;
    String studyName = "Test Study No Image";
    Integer maxMembers = 5;
    Boolean hasFee = false;
    Integer amount = 0;
    String description = "Test Description No Image";
    Set<Category> categories = Set.of(Category.LANGUAGE);
    Set<Style> styles = Set.of(Style.LIGHT_AND_FLEXIBLE);
    Set<String> regionCodes = Set.of("BUSAN");

    CreateStudyRequest request = new CreateStudyRequest(
        studyName, maxMembers, hasFee, amount, description, categories, styles, regionCodes
    );
    MultipartFile imageFile = new MockMultipartFile("image", "", "image/jpeg",
        new byte[0]); // Empty file

    when(idGenerator.nextId()).thenReturn(100L, 101L, 102L, 103L);
    when(studyRepository.save(any(Study.class))).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(studyCategoryRepository.saveAll(any())).thenAnswer(
        invocation -> invocation.getArgument(0));
    when(studyStyleRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
    when(studyRegionRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

    // when
    createStudyService.createStudy(request, leaderId, imageFile);

    // then
    verify(fileStoragePort, times(0)).upload(any(MultipartFile.class),
        anyString()); // Should not call upload
    verify(studyRepository).save(studyCaptor.capture());

    Study capturedStudy = studyCaptor.getValue();
    assertThat(capturedStudy.getId()).isEqualTo(100L);
    assertThat(capturedStudy.getLeaderId()).isEqualTo(leaderId);
    assertThat(capturedStudy.getName()).isEqualTo(studyName);
    assertThat(capturedStudy.getImageUrl()).isNull(); // Image URL should be null
  }
}
