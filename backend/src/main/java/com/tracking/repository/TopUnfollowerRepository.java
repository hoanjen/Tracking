package com.tracking.repository;

import com.tracking.entity.TopUnfollower;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopUnfollowerRepository extends JpaRepository<TopUnfollower, Long> {

    List<TopUnfollower> findByTikTokAccountIdOrderByUnfollowCountDesc(Long tikTokAccountId);

    Optional<TopUnfollower> findByTikTokAccountIdAndUsername(Long tikTokAccountId, String username);

    /** Tất cả unfollowers thuộc mọi TikTok account của một user, sắp xếp theo unfollowCount giảm dần */
    @Query("SELECT u FROM TopUnfollower u WHERE u.tikTokAccount.user.id = :userId " +
           "ORDER BY u.unfollowCount DESC")
    List<TopUnfollower> findAllByUserId(Long userId);
}
