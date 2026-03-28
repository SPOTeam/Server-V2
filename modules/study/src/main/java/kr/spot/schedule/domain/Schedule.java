package kr.spot.schedule.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import kr.spot.code.status.ErrorStatus;
import kr.spot.domain.BaseEntity;
import kr.spot.exception.GeneralException;
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

  private Long creatorId;

  @Column(nullable = false)
  private String title;

  private String locationMemo;

  private LocalDateTime startAt;

  private LocalDateTime endAt;

  private boolean attendanceActive;

  private String attendanceQrCodeImageUrl;

  public static Schedule of(Long id, Long studyId, long creatorId, String title, String locationMemo,
      LocalDateTime startAt, LocalDateTime endAt
  ) {
    return new Schedule(id, studyId, creatorId, title, locationMemo, startAt, endAt, false, null);
  }

  public void delete(long studyId) {
    validateIsValidAccess(studyId);
    super.delete();
  }

  public boolean isOngoing(LocalDateTime now) {
    if (startAt == null || endAt == null) {
      return false;
    }
    return !now.isBefore(startAt) && !now.isAfter(endAt);
  }

  public void startAttendance(long studyId) {
    validateIsValidAccess(studyId);
    validateIsOngoing();
    validateIsNotAttendanceActive();
    this.attendanceActive = true;
  }

  public void stopAttendance(long studyId) {
    validateIsValidAccess(studyId);
    this.attendanceActive = false;
    this.attendanceQrCodeImageUrl = null;
  }

  public void updateQrCodeImageUrl(String url) {
    this.attendanceQrCodeImageUrl = url;
  }

  public void validateAttendanceCheckable() {
    validateIsAttendanceActive();
    validateIsOngoing();
  }

  private void validateIsValidAccess(long studyId) {
    if (studyId != this.studyId) {
      throw new GeneralException(ErrorStatus._SCHEDULE_ACCESS_DENIED);
    }
  }

  private void validateIsOngoing() {
    if (!isOngoing(LocalDateTime.now())) {
      throw new GeneralException(ErrorStatus._ATTENDANCE_NOT_IN_SCHEDULE_TIME);
    }
  }

  private void validateIsAttendanceActive() {
    if (!this.attendanceActive) {
      throw new GeneralException(ErrorStatus._ATTENDANCE_NOT_STARTED);
    }
  }

  private void validateIsNotAttendanceActive() {
    if (this.attendanceActive) {
      throw new GeneralException(ErrorStatus._SCHEDULE_QR_CODE_ALREADY_ASSIGNED);
    }
  }
}
