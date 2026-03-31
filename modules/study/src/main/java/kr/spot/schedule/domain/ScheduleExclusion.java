package kr.spot.schedule.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE schedule_exclusion SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScheduleExclusion extends BaseEntity {

  @Id
  private Long id;

  private Long scheduleId;

  private Long memberId;

  public static ScheduleExclusion of(long id, long scheduleId, long memberId) {
    return new ScheduleExclusion(id, scheduleId, memberId);
  }
}
