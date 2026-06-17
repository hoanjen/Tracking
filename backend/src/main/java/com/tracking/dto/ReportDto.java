package com.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDto {
    private Long id;
    private Long totalFollowers;
    private LocalDateTime createdAt;
    private List<FollowerChangeDto> followerChanges;
    private ReportComparisonDto comparison;
    /**
     * true nếu đây là lần tracking đầu tiên của account này —
     * chưa có dữ liệu so sánh, không hiển thị +/- trên frontend.
     */
    private boolean isFirstTracking;
}
