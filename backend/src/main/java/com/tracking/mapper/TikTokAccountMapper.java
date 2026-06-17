package com.tracking.mapper;

import com.tracking.dto.TikTokAccountDto;
import com.tracking.entity.TikTokAccount;
import org.springframework.stereotype.Component;

@Component
public class TikTokAccountMapper {
    public TikTokAccountDto toDto(TikTokAccount account) {
        if (account == null) return null;
        return new TikTokAccountDto(
                account.getId(),
                account.getUsername(),
                account.getCreatedAt(),
                account.getTrackings() != null ? account.getTrackings().size() : 0
        );
    }
}
