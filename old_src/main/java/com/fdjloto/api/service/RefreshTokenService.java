package com.fdjloto.api.service;

import com.fdjloto.api.model.RefreshToken;
import com.fdjloto.api.model.User;
import com.fdjloto.api.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    public RefreshTokenService(RefreshTokenRepository repo) {
        this.repo = repo;
    }

    public String hash(String rawToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot hash refresh token", e);
        }
    }

    public RefreshToken save(User user, String rawRefreshToken, Instant expiresAt) {
        RefreshToken rt = new RefreshToken();
        rt.setId(UUID.randomUUID().toString());
        rt.setUser(user);
        rt.setTokenHash(hash(rawRefreshToken));
        rt.setCreatedAt(Instant.now());
        rt.setExpiresAt(expiresAt);
        rt.setRevokedAt(null);
        return repo.save(rt);
    }

    public void revoke(RefreshToken rt) {
        rt.setRevokedAt(Instant.now());
        repo.save(rt);
    }
}
