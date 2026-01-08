package kr.spot.ports;

import java.util.List;

/**
 * 스터디 멤버 ID 조회 포트.
 * 알림 발송 등에서 스터디 멤버 목록을 조회할 때 사용합니다.
 */
public interface GetStudyMemberIdsPort {

    /**
     * 해당 스터디의 모든 멤버 ID를 조회합니다.
     *
     * @param studyId 스터디 ID
     * @return 멤버 ID 목록
     */
    List<Long> getMemberIdsByStudyId(long studyId);
}
