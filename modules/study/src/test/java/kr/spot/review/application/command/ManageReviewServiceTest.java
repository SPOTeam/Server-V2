package kr.spot.review.application.command;

import static kr.spot.review.common.ReviewFixture.ACTIVITY;
import static kr.spot.review.common.ReviewFixture.ENCOURAGEMENT;
import static kr.spot.review.common.ReviewFixture.LEARNED;
import static kr.spot.review.common.ReviewFixture.MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.OTHER_MEMBER_ID;
import static kr.spot.review.common.ReviewFixture.STUDY_ID;
import static kr.spot.review.common.ReviewFixture.WRITER_NAME;
import static kr.spot.review.common.ReviewFixture.WRITER_PROFILE_IMAGE_URL;
import static kr.spot.review.common.ReviewFixture.createPrivateReviewRequest;
import static kr.spot.review.common.ReviewFixture.createReviewRequest;
import static kr.spot.review.common.ReviewFixture.review;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.review.domain.Review;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import kr.spot.review.presentation.command.dto.CreateReviewRequest;
import kr.spot.study.application.validator.StudyAccessValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ManageReviewServiceTest {

  @Mock
  IdGenerator idGenerator;

  @Mock
  GetWriterInfoPort getWriterInfoPort;

  @Mock
  FileStoragePort fileStoragePort;

  @Mock
  ReviewRepository reviewRepository;

  @Mock
  StudyAccessValidator studyAccessValidator;

  @Captor
  ArgumentCaptor<Review> reviewCaptor;

  ManageReviewService manageReviewService;

  @BeforeEach
  void setUp() {
    manageReviewService = new ManageReviewService(
        idGenerator, getWriterInfoPort, fileStoragePort, reviewRepository, studyAccessValidator);
  }

  @Nested
  @DisplayName("회고 생성 (createReview)")
  class CreateReview {

    @Test
    @DisplayName("회고를 정상적으로 생성할 수 있다")
    void should_create_review_successfully() {
      // given
      Long generatedId = 1L;
      CreateReviewRequest request = createReviewRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(MEMBER_ID, WRITER_NAME,
          WRITER_PROFILE_IMAGE_URL);

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(anyLong())).thenReturn(writerInfoResponse);
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageReviewService.createReview(STUDY_ID, MEMBER_ID, request, null);

      // then
      verify(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(reviewRepository).save(reviewCaptor.capture());

      Review capturedReview = reviewCaptor.getValue();
      assertThat(capturedReview.getId()).isEqualTo(generatedId);
      assertThat(capturedReview.getStudyId()).isEqualTo(STUDY_ID);
      assertThat(capturedReview.getWriterInfo().getWriterId()).isEqualTo(MEMBER_ID);
      assertThat(capturedReview.getContent().getActivity()).isEqualTo(ACTIVITY);
      assertThat(capturedReview.getContent().getLearned()).isEqualTo(LEARNED);
      assertThat(capturedReview.getContent().getEncouragement()).isEqualTo(ENCOURAGEMENT);
      assertThat(capturedReview.isPrivate()).isFalse();
    }

    @Test
    @DisplayName("스터디원 전용 회고를 생성할 수 있다")
    void should_create_private_review_successfully() {
      // given
      Long generatedId = 1L;
      CreateReviewRequest request = createPrivateReviewRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(MEMBER_ID, WRITER_NAME,
          WRITER_PROFILE_IMAGE_URL);

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(anyLong())).thenReturn(writerInfoResponse);
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageReviewService.createReview(STUDY_ID, MEMBER_ID, request, null);

      // then
      verify(reviewRepository).save(reviewCaptor.capture());
      assertThat(reviewCaptor.getValue().isPrivate()).isTrue();
    }

    @Test
    @DisplayName("이미지와 함께 회고를 생성할 수 있다")
    void should_create_review_with_image() {
      // given
      Long generatedId = 1L;
      CreateReviewRequest request = createReviewRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(MEMBER_ID, WRITER_NAME,
          WRITER_PROFILE_IMAGE_URL);
      MultipartFile imageFile = new MockMultipartFile("image", "test.jpg", "image/jpeg",
          "test".getBytes());
      String uploadedImageUrl = "https://s3.example.com/reviews/test.jpg";

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(anyLong())).thenReturn(writerInfoResponse);
      when(fileStoragePort.upload(any(MultipartFile.class), anyString()))
          .thenReturn(new UploadResult(uploadedImageUrl, "test.jpg"));
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageReviewService.createReview(STUDY_ID, MEMBER_ID, request, List.of(imageFile));

      // then
      verify(fileStoragePort).upload(imageFile, "reviews");
      verify(reviewRepository).save(reviewCaptor.capture());
      assertThat(reviewCaptor.getValue().getContent().getImageUrls()).containsExactly(uploadedImageUrl);
    }

    @Test
    @DisplayName("이미지는 최대 3개까지만 등록할 수 있다")
    void should_throw_exception_when_images_exceed_limit() {
      // given
      CreateReviewRequest request = createReviewRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(MEMBER_ID, WRITER_NAME,
          WRITER_PROFILE_IMAGE_URL);
      MultipartFile imageFile1 = new MockMultipartFile("image1", "1.jpg", "image/jpeg",
          "1".getBytes());
      MultipartFile imageFile2 = new MockMultipartFile("image2", "2.jpg", "image/jpeg",
          "2".getBytes());
      MultipartFile imageFile3 = new MockMultipartFile("image3", "3.jpg", "image/jpeg",
          "3".getBytes());
      MultipartFile imageFile4 = new MockMultipartFile("image4", "4.jpg", "image/jpeg",
          "4".getBytes());

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(getWriterInfoPort.get(anyLong())).thenReturn(writerInfoResponse);

      // when & then
      assertThatThrownBy(() -> manageReviewService.createReview(STUDY_ID, MEMBER_ID, request,
          List.of(imageFile1, imageFile2, imageFile3, imageFile4)))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._BAD_REQUEST);

      verify(fileStoragePort, never()).upload(any(MultipartFile.class), anyString());
      verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미지 3개를 모두 등록할 수 있다")
    void should_create_review_with_three_images() {
      // given
      Long generatedId = 1L;
      CreateReviewRequest request = createReviewRequest();
      WriterInfoResponse writerInfoResponse = WriterInfoResponse.of(MEMBER_ID, WRITER_NAME,
          WRITER_PROFILE_IMAGE_URL);
      MultipartFile imageFile1 = new MockMultipartFile("image1", "1.jpg", "image/jpeg",
          "1".getBytes());
      MultipartFile imageFile2 = new MockMultipartFile("image2", "2.jpg", "image/jpeg",
          "2".getBytes());
      MultipartFile imageFile3 = new MockMultipartFile("image3", "3.jpg", "image/jpeg",
          "3".getBytes());
      String uploadedImageUrl1 = "https://s3.example.com/reviews/1.jpg";
      String uploadedImageUrl2 = "https://s3.example.com/reviews/2.jpg";
      String uploadedImageUrl3 = "https://s3.example.com/reviews/3.jpg";

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(idGenerator.nextId()).thenReturn(generatedId);
      when(getWriterInfoPort.get(anyLong())).thenReturn(writerInfoResponse);
      when(fileStoragePort.upload(any(MultipartFile.class), anyString()))
          .thenReturn(
              new UploadResult(uploadedImageUrl1, "1.jpg"),
              new UploadResult(uploadedImageUrl2, "2.jpg"),
              new UploadResult(uploadedImageUrl3, "3.jpg")
          );
      when(reviewRepository.save(any(Review.class)))
          .thenAnswer(invocation -> invocation.getArgument(0));

      // when
      manageReviewService.createReview(STUDY_ID, MEMBER_ID, request,
          List.of(imageFile1, imageFile2, imageFile3));

      // then
      verify(fileStoragePort, times(3)).upload(any(MultipartFile.class), anyString());
      verify(reviewRepository).save(reviewCaptor.capture());
      assertThat(reviewCaptor.getValue().getContent().getImageUrls())
          .containsExactly(uploadedImageUrl1, uploadedImageUrl2, uploadedImageUrl3);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 예외가 발생한다")
    void should_throw_exception_when_not_study_member() {
      // given
      CreateReviewRequest request = createReviewRequest();

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());

      // when & then
      assertThatThrownBy(
          () -> manageReviewService.createReview(STUDY_ID, MEMBER_ID, request, null))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);

      verify(reviewRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("회고 삭제 (deleteReview)")
  class DeleteReview {

    @Test
    @DisplayName("회고를 정상적으로 삭제할 수 있다")
    void should_delete_review_successfully() {
      // given
      long reviewId = 1L;
      Review review = review(reviewId, STUDY_ID, MEMBER_ID);

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(reviewRepository.getById(anyLong())).thenReturn(review);

      // when
      manageReviewService.deleteReview(STUDY_ID, reviewId, MEMBER_ID);

      // then
      verify(studyAccessValidator).validateStudyMember(STUDY_ID, MEMBER_ID);
      verify(reviewRepository).getById(reviewId);
      verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("작성자가 아니면 삭제할 수 없다")
    void should_throw_exception_when_not_author() {
      // given
      long reviewId = 1L;
      Review review = review(reviewId, STUDY_ID, MEMBER_ID);

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(reviewRepository.getById(anyLong())).thenReturn(review);

      // when & then
      assertThatThrownBy(
          () -> manageReviewService.deleteReview(STUDY_ID, reviewId, OTHER_MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._ONLY_AUTHOR_CAN_MODIFY);

      verify(reviewRepository, never()).delete(any());
    }

    @Test
    @DisplayName("존재하지 않는 회고를 삭제하려고 하면 예외가 발생한다")
    void should_throw_exception_when_review_not_found() {
      // given
      long reviewId = 999L;

      doNothing().when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());
      when(reviewRepository.getById(anyLong()))
          .thenThrow(new GeneralException(ErrorStatus._REVIEW_NOT_FOUND));

      // when & then
      assertThatThrownBy(() -> manageReviewService.deleteReview(STUDY_ID, reviewId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._REVIEW_NOT_FOUND);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 삭제할 수 없다")
    void should_throw_exception_when_not_study_member() {
      // given
      long reviewId = 1L;

      doThrow(new GeneralException(ErrorStatus._STUDY_ACCESS_DENIED))
          .when(studyAccessValidator).validateStudyMember(anyLong(), anyLong());

      // when & then
      assertThatThrownBy(() -> manageReviewService.deleteReview(STUDY_ID, reviewId, MEMBER_ID))
          .isInstanceOf(GeneralException.class)
          .hasFieldOrPropertyWithValue("status", ErrorStatus._STUDY_ACCESS_DENIED);

      verify(reviewRepository, never()).delete(any());
    }
  }
}
