package com.tracking.controller;

import com.tracking.dto.TopUnfollowerDto;
import com.tracking.repository.TopUnfollowerRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/top-unfollowers")
public class TopUnfollowerController {

    private final TopUnfollowerRepository topUnfollowerRepository;

    public TopUnfollowerController(TopUnfollowerRepository topUnfollowerRepository) {
        this.topUnfollowerRepository = topUnfollowerRepository;
    }

    /**
     * Trả về tổng hợp top unfollowers của TẤT CẢ TikTok accounts thuộc user đang đăng nhập.
     * Mỗi record kèm tên TikTok account để phân biệt.
     */
    @GetMapping
    public ResponseEntity<?> getTopUnfollowers(Authentication authentication) {
        try {
            Long userId = extractUserId(authentication);
            List<TopUnfollowerDto> result = topUnfollowerRepository
                    .findAllByUserId(userId)
                    .stream()
                    .map(u -> new TopUnfollowerDto(
                            u.getId(),
                            u.getUsername(),
                            u.getUnfollowCount(),
                            u.getTikTokAccount().getUsername()
                    ))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("Unauthorized");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) return (Long) principal;
        if (principal instanceof String) return Long.parseLong((String) principal);
        throw new RuntimeException("Invalid authentication");
    }
}
