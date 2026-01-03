package kr.spot.study.application.command;

import kr.spot.IdGenerator;
import kr.spot.study.application.event.StudyCreatedEvent;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyCategory;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.associations.StudyRegion;
import kr.spot.study.domain.associations.StudyStats;
import kr.spot.study.domain.associations.StudyStyle;
import kr.spot.study.domain.vo.Fee;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyStatsRepository;
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
public class CreateStudyService {

  private final IdGenerator idGenerator;
  private final ApplicationEventPublisher eventPublisher;
  private final StudyRepository studyRepository;

  private final StudyStyleRepository studyStyleRepository;
  private final StudyRegionRepository studyRegionRepository;
  private final StudyCategoryRepository studyCategoryRepository;
  private final StudyStatsRepository studyStatsRepository;
  private final StudyMemberRepository studyMemberRepository;

  public long createStudy(CreateStudyRequest request, long leaderId, MultipartFile imageFile) {
    long studyId = idGenerator.nextId();
    Study study = Study.of(studyId, leaderId, request.name(), request.maxMembers(),
        Fee.of(request.hasFee(), request.amount()), null, request.description(), request.isOnline());
    StudyStats studyStats = StudyStats.of(studyId);
    StudyMember studyMember = StudyMember.create(idGenerator.nextId(), studyId, leaderId);

    studyRepository.save(study);
    studyStatsRepository.save(studyStats);
    studyMemberRepository.save(studyMember);

    saveAllStudyCategories(request, studyId);
    saveAllStudyStyles(request, studyId);
    saveAllStudyRegions(request, studyId);

    eventPublisher.publishEvent(StudyCreatedEvent.of(studyId, imageFile));
    return studyId;
  }

  private void saveAllStudyCategories(CreateStudyRequest request, long studyId) {
    var studyCategories = request.categories().stream()
        .map(cat -> StudyCategory.of(idGenerator.nextId(), studyId, cat))
        .toList();
    studyCategoryRepository.saveAll(studyCategories);
  }

  private void saveAllStudyStyles(CreateStudyRequest request, long studyId) {
    var studyStyles = request.styles().stream()
        .map(style -> StudyStyle.of(idGenerator.nextId(), studyId, style))
        .toList();
    studyStyleRepository.saveAll(studyStyles);
  }

  private void saveAllStudyRegions(CreateStudyRequest request, long studyId) {
    var studyRegions = request.regionCodes().stream()
        .map(regionCode -> StudyRegion.of(idGenerator.nextId(), studyId, regionCode))
        .toList();
    studyRegionRepository.saveAll(studyRegions);
  }
}
