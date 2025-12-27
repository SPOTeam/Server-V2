package kr.spot.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.spot.domain.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE schedule SET status = 'INACTIVE' WHERE id = ?")
@SQLRestriction("status = 'ACTIVE'")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Schedule extends BaseEntity {

  @Id
  private Long id;

  private Long studyId;

  @Column(nullable = false)
  private String title;

  private String locationMemo;

  private LocalDateTime startAt;

  private LocalDateTime endAt;

  public static Schedule of(Long id, Long studyId, String title, String locationMemo,
      LocalDateTime startAt, LocalDateTime endAt
  ) {
    return new Schedule(id, studyId, title, locationMemo, startAt, endAt
    );
  }
}
