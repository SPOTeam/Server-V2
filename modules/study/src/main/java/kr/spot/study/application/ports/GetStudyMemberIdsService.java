package kr.spot.study.application.ports;

import java.util.List;
import kr.spot.ports.GetStudyMemberIdsPort;
import kr.spot.study.domain.associations.StudyMember;
import kr.spot.study.domain.enums.StudyMemberStatus;
import kr.spot.study.infrastructure.jpa.associations.StudyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudyMemberIdsService implements GetStudyMemberIdsPort {

    private static final List<StudyMemberStatus> ACTIVE_STATUSES = List.of(
        StudyMemberStatus.OWNER,
        StudyMemberStatus.APPROVED
    );

    private final StudyMemberRepository studyMemberRepository;

    @Override
    public List<Long> getMemberIdsByStudyId(long studyId) {
        return studyMemberRepository
            .findAllByStudyIdAndStudyMemberStatusIn(studyId, ACTIVE_STATUSES)
            .stream()
            .map(StudyMember::getMemberId)
            .toList();
    }
}
