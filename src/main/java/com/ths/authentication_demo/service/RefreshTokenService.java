package com.ths.authentication_demo.service;

import com.ths.authentication_demo.dto.TokenRefreshRequest;
import com.ths.authentication_demo.entity.RefreshToken;
import com.ths.authentication_demo.entity.User;
import com.ths.authentication_demo.repository.RefreshTokenRepository;
import com.ths.authentication_demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.ref.Reference;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final Long refreshTokenDurationMs = 86400000L;

    public Optional<RefreshToken> findByToken(TokenRefreshRequest request) {
        return refreshTokenRepository.findByToken(request.getRefreshToken());
    }

    @Transactional
    public RefreshToken createRefreshToken(User user) {
//        refreshTokenRepository.deleteByUser(user);
        Optional<RefreshToken> optToken  = refreshTokenRepository.findByUser(user);
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = null;
        if (optToken.isPresent()) {
            refreshToken = optToken.get();
            refreshToken.setToken(token);
        } else {
            refreshToken = RefreshToken.builder()
                    .user(user)
                    .token(UUID.randomUUID().toString())
                    .build();
        }
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs) );
        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if(token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Token expired for user");
        }
        return token;
    }
}
