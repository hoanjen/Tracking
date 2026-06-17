package com.tracking.repository;

import com.tracking.entity.Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrackingRepository extends JpaRepository<Tracking, Long> {
    List<Tracking> findByTikTokAccountId(Long tikTokAccountId);

    @Query("SELECT t FROM Tracking t WHERE t.tikTokAccount.id = :tikTokAccountId " +
           "ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Tracking> findLatestByTikTokAccountId(Long tikTokAccountId);

    @Query("SELECT t FROM Tracking t WHERE t.tikTokAccount.id = :tikTokAccountId " +
           "AND t.id <> :excludeId ORDER BY t.createdAt DESC LIMIT 1")
    Optional<Tracking> findPreviousByTikTokAccountId(Long tikTokAccountId, Long excludeId);

    @Query("SELECT COUNT(t) FROM Tracking t WHERE t.tikTokAccount.id = :tikTokAccountId " +
           "AND CAST(t.createdAt AS DATE) = CAST(CURRENT_TIMESTAMP AS DATE)")
    Integer countTodayByTikTokAccountId(Long tikTokAccountId);
}
