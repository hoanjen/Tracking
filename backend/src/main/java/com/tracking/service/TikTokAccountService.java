package com.tracking.service;

import com.tracking.dto.CreateTikTokAccountRequest;
import com.tracking.dto.TikTokAccountDto;
import com.tracking.entity.TikTokAccount;
import com.tracking.entity.User;
import com.tracking.mapper.TikTokAccountMapper;
import com.tracking.repository.TikTokAccountRepository;
import com.tracking.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TikTokAccountService {

    private final TikTokAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TikTokAccountMapper mapper;

    public TikTokAccountService(TikTokAccountRepository accountRepository, UserRepository userRepository,
                                TikTokAccountMapper mapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    public TikTokAccountDto createAccount(Long userId, CreateTikTokAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean exists = accountRepository.findByUsernameAndUserId(request.getUsername(), userId).isPresent();
        if (exists) {
            throw new RuntimeException("TikTok account already added");
        }

        TikTokAccount account = TikTokAccount.builder()
                .username(request.getUsername())
                .user(user)
                .build();

        return mapper.toDto(accountRepository.save(account));
    }

    public List<TikTokAccountDto> getUserAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteAccount(Long userId, Long accountId) {
        TikTokAccount account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        accountRepository.delete(account);
    }
}
