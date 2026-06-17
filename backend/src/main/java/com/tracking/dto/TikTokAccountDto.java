package com.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TikTokAccountDto {
    private Long id;
    private String username;
    private LocalDateTime createdAt;
    private Integer trackingCount;
}
