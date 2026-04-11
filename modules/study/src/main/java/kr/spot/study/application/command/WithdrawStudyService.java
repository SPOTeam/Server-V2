package kr.spot.study.application.command;

import jakarta.transaction.Transactional;
import java.util.List;
import kr.spot.code.status.ErrorStatus;
import kr.spot.exception.GeneralException;
import kr.spot.study.domain.Study;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.WithdrawStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawStudyService {

  private final StudyRepository studyRepository;
  private final StudyMemberRepository studyMemberRepository;

  public void withdrawStudy(long studyId, long memberId, WithdrawStudyRequest request) {
    StudyMember studyMember = studyMemberRepository.getByMemberIdAndStudyIdAndStudyMemberStatusIn(
        memberId,
        studyId,
        List.of(StudyMemberStatus.APPROVED, StudyMemberStatus.OWNER));
    Study study = studyRepository.getStudyById(studyId);

    StudyMember nextOwner = findNextOwner(studyId, request.nextOwnerId());

    studyMember.withdrawStudy(request.withdrawReason(), nextOwner);
    study.decreaseMemberCount();
  }

  public void deleteStudy(long studyId, long memberId) {
    StudyMember studyMember = studyMemberRepository.getByMemberIdAndStudyId(memberId, studyId);
    Study study = studyRepository.getStudyById(studyId);

    study.finishStudy(studyMember);
  }

  private StudyMember findNextOwner(long studyId, Long nextOwnerId) {
    if (nextOwnerId == null || nextOwnerId <= 0) {
      return null;
    }

    return studyMemberRepository.findByMemberIdAndStudyId(nextOwnerId, studyId)
        .orElseThrow(() -> new GeneralException(ErrorStatus._NEXT_OWNER_NOT_EXIST_IN_STUDY));
  }
}
