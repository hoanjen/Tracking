package com.tracking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trackings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tiktok_account_id", nullable = false)
    private TikTokAccount tikTokAccount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "tracking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Report> reports;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reports == null) {
            reports = new ArrayList<>();
        }
    }
}
