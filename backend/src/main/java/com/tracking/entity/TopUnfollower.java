package com.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "top_unfollowers", uniqueConstraints = @UniqueConstraint(columnNames = {"tiktok_account_id", "username"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUnfollower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiktok_account_id", nullable = false)
    private TikTokAccount tikTokAccount;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer unfollowCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
