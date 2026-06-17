package com.tracking.controller;

import com.tracking.dto.ReportDto;
import com.tracking.dto.ReportComparisonDto;
import com.tracking.dto.FollowerChangeDto;
import com.tracking.entity.Report;
import com.tracking.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tracking")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Tạo tracking snapshot mới — tự động fetch followers từ TikTok API.
     * Không cần request body.
     */
    @PostMapping("/{accountId}/create-tracking")
    public ResponseEntity<?> createTracking(
            Authentication authentication,
            @PathVariable Long accountId) {
        try {
            Long userId = extractUserId(authentication);
            Report report = trackingService.createTracking(userId, accountId);
            return ResponseEntity.ok(mapToDto(report));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{accountId}/history")
    public ResponseEntity<?> getTrackingHistory(
            Authentication authentication,
            @PathVariable Long accountId) {
        try {
            Long userId = extractUserId(authentication);
            List<Report> reports = trackingService.getTrackingHistory(userId, accountId);
            List<ReportDto> dtos = reports.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) return (Long) principal;
        if (principal instanceof String) return Long.parseLong((String) principal);
        throw new RuntimeException("Invalid authentication: Cannot extract userId");
    }

    private ReportDto mapToDto(Report report) {
        List<FollowerChangeDto> changes = (report.getFollowerChanges() != null)
                ? report.getFollowerChanges().stream()
                        // "current" chỉ dùng nội bộ, không gửi ra ngoài
                        .filter(f -> !"current".equals(f.getChangeType()))
                        .map(f -> new FollowerChangeDto(f.getId(), f.getUsername(), f.getChangeType()))
                        .collect(Collectors.toList())
                : List.of();

        long added   = changes.stream().filter(f -> "added".equals(f.getChangeType())).count();
        long removed = changes.stream().filter(f -> "removed".equals(f.getChangeType())).count();

        // Lần đầu tiên: không có entry nào là added/removed
        boolean isFirstTracking = report.getFollowerChanges() == null
                || report.getFollowerChanges().stream()
                        .noneMatch(f -> "added".equals(f.getChangeType()) || "removed".equals(f.getChangeType()));

        ReportComparisonDto comparison = isFirstTracking ? null : new ReportComparisonDto(added, removed);

        return new ReportDto(
                report.getId(),
                report.getTotalFollowers(),
                report.getCreatedAt(),
                changes,
                comparison,
                isFirstTracking
        );
    }
}
