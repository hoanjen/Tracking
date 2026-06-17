package com.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopUnfollowerDto {
    private Long id;
    private String username;
    private Integer unfollowCount;
    /** TikTok account đang được track (để phân biệt khi hiển thị tổng hợp) */
    private String tikTokUsername;
}
