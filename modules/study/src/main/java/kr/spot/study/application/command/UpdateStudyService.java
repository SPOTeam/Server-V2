package kr.spot.study.application.command;

import java.util.List;
import kr.spot.IdGenerator;
import kr.spot.study.application.event.StudyUpdatedEvent;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyCategory;
import kr.spot.study.domain.associations.StudyRegion;
import kr.spot.study.domain.associations.StudyStyle;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.study.presentation.command.dto.request.CreateStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class UpdateStudyService {

  private final IdGenerator idGenerator;
  private final ApplicationEventPublisher eventPublisher;
  private final StudyRepository studyRepository;

  private final StudyStyleRepository studyStyleRepository;
  private final StudyRegionRepository studyRegionRepository;
  private final StudyCategoryRepository studyCategoryRepository;

  public void updateStudy(long studyId, CreateStudyRequest request, long requesterId,
      MultipartFile imageFile) {
    Study study = studyRepository.getStudyById(studyId);
    study.update(requesterId, request.name(), request.maxMembers(),
        Fee.of(request.hasFee(), request.amount()), request.description(), request.isOnline());

    replaceStudyCategories(studyId, request);
    replaceStudyStyles(studyId, request);
    replaceStudyRegions(studyId, request);

    eventPublisher.publishEvent(StudyUpdatedEvent.of(studyId, imageFile));
  }

  private void replaceStudyCategories(long studyId, CreateStudyRequest request) {
    List<StudyCategory> existing = studyCategoryRepository.findAllByStudyId(studyId);
    studyCategoryRepository.deleteAll(existing);
    List<StudyCategory> created = request.categories().stream()
        .map(cat -> StudyCategory.of(idGenerator.nextId(), studyId, cat))
        .toList();
    studyCategoryRepository.saveAll(created);
  }

  private void replaceStudyStyles(long studyId, CreateStudyRequest request) {
    List<StudyStyle> existing = studyStyleRepository.findAllByStudyId(studyId);
    studyStyleRepository.deleteAll(existing);
    List<StudyStyle> created = request.styles().stream()
        .map(style -> StudyStyle.of(idGenerator.nextId(), studyId, style))
        .toList();
    studyStyleRepository.saveAll(created);
  }

  private void replaceStudyRegions(long studyId, CreateStudyRequest request) {
    List<StudyRegion> existing = studyRegionRepository.findAllByStudyId(studyId);
    studyRegionRepository.deleteAll(existing);
    List<StudyRegion> created = request.regionCodes().stream()
        .map(regionCode -> StudyRegion.of(idGenerator.nextId(), studyId, regionCode))
        .toList();
    studyRegionRepository.saveAll(created);
  }
}
