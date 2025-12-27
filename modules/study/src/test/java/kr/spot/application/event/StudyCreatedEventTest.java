package kr.spot.application.event;

import static org.assertj.core.api.Assertions.assertThat;

import kr.spot.study.application.event.StudyCreatedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class StudyCreatedEventTest {

  @Nested
  @DisplayName("이벤트 생성 (of)")
  class CreateEvent {

    @Test
    @DisplayName("studyId와 imageFile로 이벤트를 생성할 수 있다")
    void should_create_event_with_study_id_and_image_file() {
      // given
      Long studyId = 1L;
      MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
          "content".getBytes());

      // when
      StudyCreatedEvent event = StudyCreatedEvent.of(studyId, imageFile);

      // then
      assertThat(event.studyId()).isEqualTo(studyId);
      assertThat(event.imageFile()).isEqualTo(imageFile);
    }
  }

  @Nested
  @DisplayName("이미지 존재 확인 (hasImage)")
  class HasImage {

    @Test
    @DisplayName("이미지 파일이 있으면 true를 반환한다")
    void should_return_true_when_image_file_exists() {
      // given
      MockMultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
          "content".getBytes());
      StudyCreatedEvent event = StudyCreatedEvent.of(1L, imageFile);

      // when & then
      assertThat(event.hasImage()).isTrue();
    }

    @Test
    @DisplayName("이미지 파일이 null이면 false를 반환한다")
    void should_return_false_when_image_file_is_null() {
      // given
      StudyCreatedEvent event = StudyCreatedEvent.of(1L, null);

      // when & then
      assertThat(event.hasImage()).isFalse();
    }

    @Test
    @DisplayName("이미지 파일이 비어있으면 false를 반환한다")
    void should_return_false_when_image_file_is_empty() {
      // given
      MockMultipartFile emptyFile = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);
      StudyCreatedEvent event = StudyCreatedEvent.of(1L, emptyFile);

      // when & then
      assertThat(event.hasImage()).isFalse();
    }
  }
}
