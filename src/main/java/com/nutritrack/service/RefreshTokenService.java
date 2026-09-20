package com.nutritrack.service;

import com.nutritrack.entity.RefreshToken;
import com.nutritrack.entity.User;
import com.nutritrack.exception.InvalidRefreshTokenException;
import com.nutritrack.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired private RefreshTokenRepository refreshTokenRepo;

    @Value("${jwt.refresh.expiration}")
    private long refreshExpirationMs;

    // Issues a brand-new refresh token, replacing any previous one for this user.
    public RefreshToken issue(User user) {
        refreshTokenRepo.deleteByUser(user);
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setToken(UUID.randomUUID().toString());
        rt.setExpiryDate(Instant.now().plusMillis(refreshExpirationMs));
        return refreshTokenRepo.save(rt);
    }

    // Validates a refresh token and returns the user it belongs to.
    // Rotates it (deletes the old one) so a leaked/replayed token can only be used once.
    public User consumeAndRotate(String token) {
        RefreshToken rt = refreshTokenRepo.findByToken(token)
                .orElseThrow(InvalidRefreshTokenException::new);
        if (rt.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepo.delete(rt);
            throw new InvalidRefreshTokenException();
        }
        User user = rt.getUser();
        refreshTokenRepo.delete(rt);
        return user;
    }

    public void revokeAllForUser(User user) {
        refreshTokenRepo.deleteByUser(user);
    }
}
