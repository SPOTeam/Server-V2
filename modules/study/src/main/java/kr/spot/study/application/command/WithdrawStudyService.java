package kr.spot.study.application.command;

import jakarta.transaction.Transactional;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import kr.spot.study.presentation.command.dto.request.WithdrawStudyRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class WithdrawStudyService {

  private final StudyMemberRepository studyMemberRepository;

  public void withdrawStudy(Long studyId, Long memberId, WithdrawStudyRequest request) {
    StudyMember studyMember = studyMemberRepository.getByMemberIdAndStudyId(memberId,
        studyId);

    StudyMember nextOwner = studyMemberRepository.findById(request.nextOwnerId()).orElse(null);

    studyMember.withdrawStudy(request.withdrawReason(), nextOwner);
  }
}
