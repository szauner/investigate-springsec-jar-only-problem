package com.company.service.persistence;

import java.util.Optional;

import com.company.service.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetToken, Long> {

    public Optional<PasswordResetToken> findByTokenValue(String token);

    public Optional<PasswordResetToken> findByUserId(Long userId);
}