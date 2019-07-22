package com.company.service.business;

import org.apache.commons.lang3.tuple.Pair;

import com.company.service.model.PasswordResetToken;
import com.company.service.model.TokenOperationResult;
import com.company.service.model.User;
import com.company.service.persistence.PasswordResetTokenRepository;
import com.company.service.util.OperationNotSuccessfulException;

public interface PasswordResetManager {

    public PasswordResetToken generateSecurityToken(final User user, long expirationTime,
            final PasswordResetTokenRepository tokenRepository) throws OperationNotSuccessfulException;

    public Pair<TokenOperationResult, PasswordResetToken> verifyToken(final String tokenValue,
                                                     final PasswordResetTokenRepository tokenRepository);

    public PasswordResetToken generatePasswordResetToken(final User user) throws OperationNotSuccessfulException;

    public User getUserForToken(String tokenValue);

    public TokenOperationResult resetPassword(String tokenValue, String newPassword);
}