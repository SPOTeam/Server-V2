package kr.spot.study.application.ports;

import java.util.List;
import kr.spot.ports.StudyParticipationInfoPort;
import kr.spot.ports.dto.StudyParticipationInfo;
import kr.spot.study.domain.enums.RecruitingStatus;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GetStudyParticipationInfoService implements StudyParticipationInfoPort {

  private final StudyMemberRepository studyMemberRepository;
  private final StudyRepository studyRepository;

  @Override
  public StudyParticipationInfo getStudyParticipationInfo(long memberId) {
    long participatingStudyCount = studyMemberRepository.countByMemberIdAndStudyMemberStatusIn(
        memberId,
        List.of(StudyMemberStatus.OWNER, StudyMemberStatus.APPROVED)
    );

    long recruitingStudyCount = studyRepository.countByLeaderIdAndRecruitingStatus(
        memberId,
        RecruitingStatus.RECRUITING
    );

    long appliedStudyCount = studyMemberRepository.countByMemberIdAndStudyMemberStatus(
        memberId,
        StudyMemberStatus.APPLIED
    );

    return StudyParticipationInfo.from(
        participatingStudyCount,
        recruitingStudyCount,
        appliedStudyCount
    );
  }
}
