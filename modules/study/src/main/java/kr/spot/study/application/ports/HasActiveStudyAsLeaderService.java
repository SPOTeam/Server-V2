package kr.spot.study.application.ports;

import kr.spot.ports.HasActiveStudyAsLeaderPort;
import kr.spot.study.infrastructure.jpa.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class HasActiveStudyAsLeaderService implements HasActiveStudyAsLeaderPort {

  private final StudyRepository studyRepository;

  @Override
  public boolean hasActiveStudyAsLeader(long memberId) {
    return studyRepository.existsByLeaderId(memberId);
  }
}
