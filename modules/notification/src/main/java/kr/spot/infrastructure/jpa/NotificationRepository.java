package kr.spot.infrastructure.jpa;

import java.time.LocalDateTime;
import java.util.List;
import kr.spot.domain.Notification;
import kr.spot.domain.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

  void deleteByMemberId(long memberId);

  /**
   * 발송 대상 알림 선점 (FOR UPDATE SKIP LOCKED 사용) MySQL 8.0+에서 지원
   */
  @Modifying
  @Query(value = """
      UPDATE notification n
      SET n.dispatch_status = 'PROCESSING',
          n.picked_by = :serverId,
          n.picked_at = :now
      WHERE n.id IN (
          SELECT id FROM (
              SELECT id FROM notification
              WHERE dispatch_status = 'PENDING'
                AND scheduled_at <= :now
                AND picked_by IS NULL
              ORDER BY scheduled_at ASC
              LIMIT :limit
              FOR UPDATE SKIP LOCKED
          ) AS subquery
      )
      """, nativeQuery = true)
  int pickPendingNotifications(
      @Param("serverId") String serverId,
      @Param("now") LocalDateTime now,
      @Param("limit") int limit
  );

  /**
   * 특정 서버가 선점한 알림 목록 조회
   */
  List<Notification> findByPickedByAndDispatchStatus(String serverId, NotificationStatus status);
}
