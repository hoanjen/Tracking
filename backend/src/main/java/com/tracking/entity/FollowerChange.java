package com.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "follower_changes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowerChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String changeType;

    @Column(name = "previous_report_id")
    private Long previousReportId;
}
