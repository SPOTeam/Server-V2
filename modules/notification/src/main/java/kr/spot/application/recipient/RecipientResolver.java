package kr.spot.application.recipient;

import java.util.List;
import java.util.Map;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.enums.NotificationType;
import kr.spot.exception.GeneralException;
import kr.spot.ports.GetStudyMemberIdsPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipientResolver {

  private final GetStudyMemberIdsPort getStudyMemberIdsPort;

  public List<Long> resolve(NotificationType type, Map<String, Object> payload) {
    return switch (type) {
      // 특정 회원 1명에게 발송
      case STUDY_APPLICATION_APPROVED,
           STUDY_APPLICATION_REJECTED,
           TODO_COMPLETED -> resolveSingleMember(payload);

      // 스터디 전체 멤버에게 발송
      case ATTENDANCE_STARTED,
           ATTENDANCE_ENDED,
           NOTICE_CREATED,
           SCHEDULE_CREATED,
           SCHEDULE_UPDATED,
           SCHEDULE_REMINDER -> resolveStudyMembers(payload);

      // 특정 조건의 회원들에게 발송 (추후 확장)
      case HOT_POST_DAILY -> List.of();
    };
  }

  private List<Long> resolveSingleMember(Map<String, Object> payload) {
    Object memberId = payload.get("memberId");
    if (memberId == null) {
      throw new GeneralException(ErrorStatus._NOTIFICATION_PAYLOAD_MISSING_MEMBER_ID);
    }
    return List.of(toLong(memberId));
  }

  private List<Long> resolveStudyMembers(Map<String, Object> payload) {
    Object studyId = payload.get("studyId");
    if (studyId == null) {
      throw new GeneralException(ErrorStatus._NOTIFICATION_PAYLOAD_MISSING_STUDY_ID);
    }
    return getStudyMemberIdsPort.getMemberIdsByStudyId(toLong(studyId));
  }

  private long toLong(Object value) {
    if (value instanceof Long l) {
      return l;
    }
    if (value instanceof Number n) {
      return n.longValue();
    }
    return Long.parseLong(String.valueOf(value));
  }
}
