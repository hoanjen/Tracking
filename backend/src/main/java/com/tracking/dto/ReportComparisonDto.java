package com.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportComparisonDto {
    private long followersAdded;
    private long followersRemoved;
    private long netChange;

    public ReportComparisonDto(long added, long removed) {
        this.followersAdded = added;
        this.followersRemoved = removed;
        this.netChange = added - removed;
    }
}
