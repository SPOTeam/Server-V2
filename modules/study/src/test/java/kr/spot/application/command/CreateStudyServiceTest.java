package kr.spot.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import kr.spot.IdGenerator;
import kr.spot.study.application.command.CreateStudyService;
import kr.spot.study.application.event.StudyCreatedEvent;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.enums.Category;
import kr.spot.study.domain.enums.Style;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStatsRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.study.presentation.command.dto.request.CreateStudyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class CreateStudyServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  ApplicationEventPublisher eventPublisher;

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

  @Captor
  ArgumentCaptor<StudyCreatedEvent> eventCaptor;

  CreateStudyService createStudyService;

  @BeforeEach
  void setUp() {
    createStudyService = new CreateStudyService(idGenerator, eventPublisher, studyRepository,
        studyStyleRepository, studyRegionRepository, studyCategoryRepository, studyStatsRepository,
        studyMemberRepository);
  }

  @Nested
  @DisplayName("스터디 생성 (createStudy)")
  class CreateStudy {

    @Test
    @DisplayName("이미지 파일과 함께 스터디를 생성하면 이벤트가 발행된다")
    void should_publish_event_when_create_study_with_image() {
      // given
      Long leaderId = 1L;
      CreateStudyRequest request = createStudyRequest();
      MultipartFile imageFile = new MockMultipartFile("image", "study.jpg", "image/jpeg",
          "study image content".getBytes());

      when(idGenerator.nextId()).thenReturn(100L, 101L, 102L, 103L);
      when(studyRepository.save(any(Study.class))).thenAnswer(
          invocation -> invocation.getArgument(0));

      // when
      createStudyService.createStudy(request, leaderId, imageFile);

      // then
      verify(studyRepository).save(studyCaptor.capture());
      verify(eventPublisher).publishEvent(eventCaptor.capture());

      Study capturedStudy = studyCaptor.getValue();
      assertThat(capturedStudy.getId()).isEqualTo(100L);
      assertThat(capturedStudy.getLeaderId()).isEqualTo(leaderId);
      assertThat(capturedStudy.getName()).isEqualTo(request.name());
      assertThat(capturedStudy.getImageUrl()).isNull();

      StudyCreatedEvent capturedEvent = eventCaptor.getValue();
      assertThat(capturedEvent.studyId()).isEqualTo(100L);
      assertThat(capturedEvent.imageFile()).isEqualTo(imageFile);
    }

    @Test
    @DisplayName("이미지 파일 없이 스터디를 생성해도 이벤트가 발행된다")
    void should_publish_event_when_create_study_without_image() {
      // given
      Long leaderId = 1L;
      CreateStudyRequest request = createStudyRequest();
      MultipartFile emptyFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

      when(idGenerator.nextId()).thenReturn(100L, 101L, 102L, 103L);
      when(studyRepository.save(any(Study.class))).thenAnswer(
          invocation -> invocation.getArgument(0));

      // when
      createStudyService.createStudy(request, leaderId, emptyFile);

      // then
      verify(studyRepository).save(studyCaptor.capture());
      verify(eventPublisher).publishEvent(eventCaptor.capture());

      Study capturedStudy = studyCaptor.getValue();
      assertThat(capturedStudy.getImageUrl()).isNull();

      StudyCreatedEvent capturedEvent = eventCaptor.getValue();
      assertThat(capturedEvent.studyId()).isEqualTo(100L);
      assertThat(capturedEvent.hasImage()).isFalse();
    }

    @Test
    @DisplayName("스터디 생성 시 관련 엔티티들이 모두 저장된다")
    void should_save_all_related_entities() {
      // given
      Long leaderId = 1L;
      CreateStudyRequest request = createStudyRequest();
      MultipartFile imageFile = new MockMultipartFile("image", "study.jpg", "image/jpeg",
          "content".getBytes());

      when(idGenerator.nextId()).thenReturn(100L, 101L, 102L, 103L);
      when(studyRepository.save(any(Study.class))).thenAnswer(
          invocation -> invocation.getArgument(0));

      // when
      createStudyService.createStudy(request, leaderId, imageFile);

      // then
      verify(studyRepository).save(any(Study.class));
      verify(studyStatsRepository).save(any());
      verify(studyMemberRepository).save(any());
      verify(studyCategoryRepository).saveAll(any());
      verify(studyStyleRepository).saveAll(any());
      verify(studyRegionRepository).saveAll(any());
    }

    private CreateStudyRequest createStudyRequest() {
      return new CreateStudyRequest(
          "Test Study",
          10,
          true,
          10000,
          "Test Description",
          Set.of(Category.LANGUAGE),
          Set.of(Style.DISCUSSION_BASED),
          Set.of("SEOUL")
      );
    }
  }
}
