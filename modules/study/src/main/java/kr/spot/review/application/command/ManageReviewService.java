package kr.spot.review.application.command;

import kr.spot.IdGenerator;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.GetWriterInfoPort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.ports.dto.WriterInfoResponse;
import kr.spot.review.domain.Review;
import kr.spot.review.domain.vo.Content;
import kr.spot.review.domain.vo.WriterInfo;
import kr.spot.review.infrastructure.jpa.ReviewRepository;
import kr.spot.review.presentation.command.dto.CreateReviewRequest;
import kr.spot.study.application.validator.StudyAccessValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class ManageReviewService {

  private static final String REVIEW_IMAGE_DIR = "reviews";

  private final IdGenerator idGenerator;
  private final GetWriterInfoPort getWriterInfoPort;
  private final FileStoragePort fileStoragePort;
  private final ReviewRepository reviewRepository;
  private final StudyAccessValidator studyAccessValidator;

  public long createReview(long studyId, long memberId, CreateReviewRequest request,
      MultipartFile imageFile) {
    studyAccessValidator.validateStudyMember(studyId, memberId);

    WriterInfo writerInfo = getWriterInfo(memberId);
    String imageUrl = uploadImage(imageFile);
    Content content = Content.of(request.activity(), request.learned(), request.encouragement(),
        imageUrl);

    long reviewId = idGenerator.nextId();
    Review review = Review.of(reviewId, studyId, writerInfo, content, request.isPrivate());
    reviewRepository.save(review);
    return reviewId;
  }

  public void deleteReview(long studyId, long reviewId, long memberId) {
    studyAccessValidator.validateStudyMember(studyId, memberId);

    Review review = reviewRepository.getById(reviewId);
    review.getWriterInfo().validateIsOwnMember(memberId);

    reviewRepository.delete(review);
  }

  private WriterInfo getWriterInfo(long memberId) {
    WriterInfoResponse response = getWriterInfoPort.get(memberId);
    return WriterInfo.of(response.writerId(), response.nickname(), response.profileImageUrl());
  }

  private String uploadImage(MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return null;
    }
    UploadResult result = fileStoragePort.upload(imageFile, REVIEW_IMAGE_DIR);
    return result.url();
  }
}
