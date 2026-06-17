package com.tracking.service;

import com.tracking.entity.TikTokAccount;
import com.tracking.entity.Tracking;
import com.tracking.entity.Report;
import com.tracking.entity.FollowerChange;
import com.tracking.entity.TopUnfollower;
import com.tracking.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@Slf4j
public class TrackingService {

    private final TrackingRepository trackingRepository;
    private final TikTokAccountRepository accountRepository;
    private final ReportRepository reportRepository;
    private final TopUnfollowerRepository topUnfollowerRepository;
    private final TikTokApiService tikTokApiService;

    public TrackingService(TrackingRepository trackingRepository,
                           TikTokAccountRepository accountRepository,
                           ReportRepository reportRepository,
                           TopUnfollowerRepository topUnfollowerRepository,
                           TikTokApiService tikTokApiService) {
        this.trackingRepository = trackingRepository;
        this.accountRepository = accountRepository;
        this.reportRepository = reportRepository;
        this.topUnfollowerRepository = topUnfollowerRepository;
        this.tikTokApiService = tikTokApiService;
    }

    /**
     * Tự động fetch followers của TikTok username từ API,
     * sau đó tạo tracking snapshot và so sánh với lần trước.
     */
    @Transactional
    public Report createTracking(Long userId, Long accountId) {
        TikTokAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Account does not belong to user");
        }

        Integer todayCount = trackingRepository.countTodayByTikTokAccountId(accountId);
        if (todayCount != null && todayCount >= 5) {
            throw new RuntimeException("Daily tracking limit (5) exceeded");
        }

        // Fetch followers trực tiếp từ TikTok API
        Set<String> currentFollowers = tikTokApiService.fetchFollowers(account.getUsername());
        log.debug("Fetched {} followers for @{}", currentFollowers.size(), account.getUsername());

        long totalFollowers = currentFollowers.size();

        Tracking tracking = Tracking.builder()
                .tikTokAccount(account)
                .build();
        tracking = trackingRepository.save(tracking);

        Report report = Report.builder()
                .tracking(tracking)
                .totalFollowers(totalFollowers)
                .followerChanges(new ArrayList<>())
                .build();
        report = reportRepository.save(report);

        // Tìm tracking trước đó (loại trừ tracking vừa tạo)
        Long currentTrackingId = tracking.getId();
        Optional<Tracking> previousTracking = trackingRepository
                .findPreviousByTikTokAccountId(accountId, currentTrackingId);

        if (previousTracking.isPresent()) {
            Optional<Report> previousReport = reportRepository.findLatestByTrackingId(previousTracking.get().getId());
            if (previousReport.isPresent()) {
                Set<String> previousFollowers = getSnapshotFollowers(previousReport.get());
                buildFollowerChanges(report, currentFollowers, previousFollowers, account);
            } else {
                storeCurrentSnapshot(report, currentFollowers);
            }
        } else {
            // Lần đầu tiên — chỉ lưu snapshot, chưa có so sánh
            storeCurrentSnapshot(report, currentFollowers);
        }

        return report;
    }

    /**
     * Lưu toàn bộ follower hiện tại dưới dạng "current" snapshot.
     * Dùng cho lần tracking đầu tiên.
     */
    private void storeCurrentSnapshot(Report report, Set<String> currentFollowers) {
        for (String username : currentFollowers) {
            FollowerChange entry = FollowerChange.builder()
                    .report(report)
                    .username(username)
                    .changeType("current")
                    .build();
            report.getFollowerChanges().add(entry);
        }
        reportRepository.save(report);
    }

    /**
     * Lấy toàn bộ follower từ snapshot của report cũ (changeType = "current").
     */
    private Set<String> getSnapshotFollowers(Report report) {
        return new HashSet<>(report.getFollowerChanges().stream()
                .filter(f -> "current".equals(f.getChangeType()))
                .map(FollowerChange::getUsername)
                .toList());
    }

    /**
     * So sánh follower hiện tại vs lần trước.
     * Lưu added/removed + snapshot "current" để lần sau so sánh tiếp.
     */
    private void buildFollowerChanges(Report currentReport, Set<String> currentFollowers,
                                      Set<String> previousFollowers, TikTokAccount account) {
        // Người mới follow
        for (String username : currentFollowers) {
            if (!previousFollowers.contains(username)) {
                currentReport.getFollowerChanges().add(FollowerChange.builder()
                        .report(currentReport)
                        .username(username)
                        .changeType("added")
                        .build());
            }
        }

        // Người đã unfollow
        for (String username : previousFollowers) {
            if (!currentFollowers.contains(username)) {
                currentReport.getFollowerChanges().add(FollowerChange.builder()
                        .report(currentReport)
                        .username(username)
                        .changeType("removed")
                        .build());
                updateTopUnfollower(account, username);
            }
        }

        // Snapshot đầy đủ cho lần so sánh tiếp theo
        for (String username : currentFollowers) {
            currentReport.getFollowerChanges().add(FollowerChange.builder()
                    .report(currentReport)
                    .username(username)
                    .changeType("current")
                    .build());
        }

        reportRepository.save(currentReport);
    }

    private void updateTopUnfollower(TikTokAccount account, String username) {
        Optional<TopUnfollower> existing = topUnfollowerRepository
                .findByTikTokAccountIdAndUsername(account.getId(), username);

        if (existing.isPresent()) {
            TopUnfollower u = existing.get();
            u.setUnfollowCount(u.getUnfollowCount() + 1);
            topUnfollowerRepository.save(u);
        } else {
            topUnfollowerRepository.save(TopUnfollower.builder()
                    .tikTokAccount(account)
                    .username(username)
                    .unfollowCount(1)
                    .build());
        }
    }

    /**
     * Lấy toàn bộ lịch sử tracking của một TikTok account.
     */
    public List<Report> getTrackingHistory(Long userId, Long accountId) {
        TikTokAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Account does not belong to user");
        }

        return reportRepository.findAllByTikTokAccountIdOrderByCreatedAtDesc(accountId);
    }
}
