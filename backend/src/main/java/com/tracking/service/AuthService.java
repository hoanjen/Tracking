package com.tracking.service;

import com.tracking.dto.LoginRequest;
import com.tracking.dto.LoginResponse;
import com.tracking.dto.UserDto;
import com.tracking.entity.User;
import com.tracking.mapper.UserMapper;
import com.tracking.repository.UserRepository;
import com.tracking.security.JwtTokenProvider;
import com.tracking.util.GoogleTokenVerifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository, JwtTokenProvider tokenProvider,
                      GoogleTokenVerifier googleTokenVerifier, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.googleTokenVerifier = googleTokenVerifier;
        this.userMapper = userMapper;
    }

    public LoginResponse login(LoginRequest request) throws Exception {
        Map<String, String> tokenInfo = googleTokenVerifier.verifyToken(request.getGoogleToken());
        String googleId = tokenInfo.get("sub");
        String email = tokenInfo.get("email");
        String name = tokenInfo.get("name");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .googleId(googleId)
                            .email(email)
                            .name(name)
                            .build();
                    return userRepository.save(newUser);
                });

        String accessToken = tokenProvider.generateToken(user.getId(), user.getEmail());
        UserDto userDto = userMapper.toDto(user);

        return new LoginResponse(
                accessToken,
                "Bearer",
                tokenProvider.getExpirationSeconds(),
                userDto
        );
    }

    public UserDto getCurrentUser(Long userId) {
        return userRepository.findById(userId)
                .map(userMapper::toDto)
                .orElse(null);
    }
}
