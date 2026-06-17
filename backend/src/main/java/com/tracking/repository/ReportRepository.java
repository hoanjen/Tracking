package com.tracking.repository;

import com.tracking.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByTrackingIdOrderByCreatedAtDesc(Long trackingId);

    @Query("SELECT r FROM Report r WHERE r.tracking.id = :trackingId " +
           "ORDER BY r.createdAt DESC LIMIT 1")
    Optional<Report> findLatestByTrackingId(Long trackingId);

    @Query("SELECT r FROM Report r WHERE r.tracking.id = :trackingId " +
           "ORDER BY r.createdAt DESC LIMIT 2")
    List<Report> findLastTwoByTrackingId(Long trackingId);

    @Query("SELECT r FROM Report r WHERE r.tracking.tikTokAccount.id = :accountId " +
           "ORDER BY r.createdAt DESC")
    List<Report> findAllByTikTokAccountIdOrderByCreatedAtDesc(Long accountId);
}
