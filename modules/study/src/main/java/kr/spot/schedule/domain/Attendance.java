package kr.spot.schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.spot.domain.BaseEntity;
import kr.spot.schedule.domain.enums.AttendanceStatus;
import kr.spot.schedule.domain.vo.MemberInfo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE attendance SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Attendance extends BaseEntity {

  @Id
  private Long id;

  private Long scheduleId;

  private MemberInfo memberInfo;

  private AttendanceStatus attendanceStatus;

  private LocalDateTime attendedAt;

  public static Attendance of(Long id, Long scheduleId, MemberInfo memberInfo) {
    return new Attendance(id, scheduleId, memberInfo, AttendanceStatus.UNDECIDED,
        LocalDateTime.now());
  }

  public void markAttendance(AttendanceStatus status) {
    this.attendanceStatus = status;
    this.attendedAt = LocalDateTime.now();
  }
}
