package kr.spot.schedule.infrastructure.jpa;

import java.util.List;
import java.util.Optional;
import kr.spot.schedule.domain.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

  Optional<Attendance> findByScheduleIdAndMemberInfoMemberId(Long scheduleId, Long memberId);

  boolean existsByScheduleIdAndMemberInfoMemberId(Long scheduleId, Long memberId);

  List<Attendance> findAllByScheduleId(Long scheduleId);

  void deleteByMemberInfoMemberId(long memberId);
}
