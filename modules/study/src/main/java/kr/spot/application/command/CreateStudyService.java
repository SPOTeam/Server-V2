package kr.spot.application.command;

import kr.spot.IdGenerator;
import kr.spot.domain.Study;
import kr.spot.domain.associations.StudyCategory;
import kr.spot.domain.associations.StudyMember;
import kr.spot.domain.associations.StudyRegion;
import kr.spot.domain.associations.StudyStats;
import kr.spot.domain.associations.StudyStyle;
import kr.spot.domain.vo.Fee;
import kr.spot.infrastructure.jpa.StudyRepository;
import kr.spot.infrastructure.jpa.associations.StudyCategoryRepository;
import kr.spot.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.infrastructure.jpa.associations.StudyRegionRepository;
import kr.spot.infrastructure.jpa.associations.StudyStatsRepository;
import kr.spot.infrastructure.jpa.associations.StudyStyleRepository;
import kr.spot.ports.FileStoragePort;
import kr.spot.ports.dto.UploadResult;
import kr.spot.presentation.command.dto.request.CreateStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class CreateStudyService {

  private static final String FILE_DIR = "studies/images/";

  private final IdGenerator idGenerator;
  private final FileStoragePort fileStoragePort;
  private final StudyRepository studyRepository;

  private final StudyStyleRepository studyStyleRepository;
  private final StudyRegionRepository studyRegionRepository;
  private final StudyCategoryRepository studyCategoryRepository;
  private final StudyStatsRepository studyStatsRepository;
  private final StudyMemberRepository studyMemberRepository;

  public void createStudy(CreateStudyRequest request, Long leaderId, MultipartFile imageFile) {
    long studyId = idGenerator.nextId();
    String imageUrl = uploadStudyImage(imageFile);
    Study study = Study.of(studyId, leaderId, request.name(), request.maxMembers(),
        Fee.of(request.hasFee(), request.amount()), imageUrl, request.description());
    StudyStats studyStats = StudyStats.of(studyId);
    StudyMember studyMember = StudyMember.create(idGenerator.nextId(), studyId, leaderId);

    studyRepository.save(study);
    studyStatsRepository.save(studyStats);
    studyMemberRepository.save(studyMember);

    saveAllStudyCategories(request, studyId);
    saveAllStudyStyles(request, studyId);
    saveAllStudyRegions(request, studyId);
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

  private String uploadStudyImage(MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return null;
    }

    UploadResult upload = fileStoragePort.upload(imageFile, FILE_DIR);
    return upload.url();
  }
}
