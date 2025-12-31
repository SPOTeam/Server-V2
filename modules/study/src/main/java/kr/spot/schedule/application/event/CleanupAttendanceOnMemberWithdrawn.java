package kr.spot.schedule.application.event;

import kr.spot.domain.events.MemberWithdrawnEvent;
import kr.spot.schedule.infrastructure.jpa.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Order(2)
@Component
@RequiredArgsConstructor
public class CleanupAttendanceOnMemberWithdrawn {

  private final AttendanceRepository attendanceRepository;

  @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
  public void handle(MemberWithdrawnEvent event) {
    attendanceRepository.deleteByMemberInfoMemberId(event.memberId());
  }
}
