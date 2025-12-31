package kr.spot.ports;

import kr.spot.ports.dto.StudyParticipationInfo;

public interface StudyParticipationInfoPort {

  StudyParticipationInfo getStudyParticipationInfo(long memberId);
}
