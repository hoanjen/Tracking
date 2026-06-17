package com.tracking.controller;

import com.tracking.dto.CreateTikTokAccountRequest;
import com.tracking.dto.TikTokAccountDto;
import com.tracking.service.TikTokAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/tiktok-accounts")
public class TikTokAccountController {

    private final TikTokAccountService accountService;

    public TikTokAccountController(TikTokAccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<?> createAccount(
            Authentication authentication,
            @RequestBody CreateTikTokAccountRequest request) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Unauthorized"));
        }
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.createAccount(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<TikTokAccountDto>> getUserAccounts(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.getUserAccounts(userId));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(Authentication authentication, @PathVariable Long accountId) {
        Long userId = (Long) authentication.getPrincipal();
        accountService.deleteAccount(userId, accountId);
        return ResponseEntity.noContent().build();
    }
}
