package com.tracking.repository;

import com.tracking.entity.TikTokAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TikTokAccountRepository extends JpaRepository<TikTokAccount, Long> {
    List<TikTokAccount> findByUserId(Long userId);
    Optional<TikTokAccount> findByUsernameAndUserId(String username, Long userId);
}
