package com.company.service.business;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.service.model.PasswordResetToken;
import com.company.service.model.TokenOperationResult;
import com.company.service.model.User;
import com.company.service.model.UserStatus;
import com.company.service.persistence.PasswordResetTokenRepository;
import com.company.service.persistence.UserRepository;
import com.company.service.util.ApplicationPropertyAccess;
import com.company.service.util.OperationNotSuccessfulException;

@Service
public class PasswordResetManagerImpl implements PasswordResetManager {
    private final static Logger log = LogManager.getLogger(PasswordResetManagerImpl.class);

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ApplicationPropertyAccess applicationProperties;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    @Autowired
    private UserRepository userRepository;

    public PasswordResetToken generateSecurityToken(final User user, long expirationTime,
        final PasswordResetTokenRepository tokenRepository) throws OperationNotSuccessfulException {
        assert (user != null);
        assert (user.getId() != null);

        Optional<PasswordResetToken> searchResult = tokenRepository.findByUserId(user.getId());
        if (searchResult.isPresent()) {
            PasswordResetToken oldToken = searchResult.get();

            // If there already is a token that is expired, delete it and continue.
            if (oldToken.isExpired()) {
                log.debug("An old expired token has been found that will be deleted: {}.",
                    () -> oldToken.getTokenValue());
                tokenRepository.delete(oldToken);
                em.flush();
                // If there is a still valid token be optimistic and reuse it (optimistic
                // because in theory
                // the token could expire in the next seconds. But this is considered an edge
                // case.
            } else {
                log.debug("An old still valid token ({}) has been found, so it will be returned instead of creating a "
                    + "new one", () -> oldToken.getTokenValue());
                return oldToken;
            }
        }

        PasswordResetToken token = createToken();
        token.setUser(user);
        token.setTokenValue(UUID.randomUUID().toString());
        tokenRepository.save(token);

        return token;
    }

    public Pair<TokenOperationResult, PasswordResetToken> verifyToken(final String tokenValue,
        final PasswordResetTokenRepository tokenRepository) {
        // a null token can never be valid
        if (tokenValue == null)
            return Pair.of(TokenOperationResult.INVALID_TOKEN, null);

        Optional<PasswordResetToken> searchResult = tokenRepository.findByTokenValue(tokenValue);

        // If no corresponding token to the given token value could be found the token
        // value has not been invalid.
        if (!searchResult.isPresent()) {
            log.debug("Given token {} is invalid.", tokenValue);
            return Pair.of(TokenOperationResult.INVALID_TOKEN, null);
        }

        // If a token could be found it has to be checked that it is still valid
        PasswordResetToken token = searchResult.get();
        if (token.isExpired()) {
            // expired tokens can be deleted
            tokenRepository.delete(token);
            return Pair.of(TokenOperationResult.TOKEN_EXPIRED, null);
        }

        // token is OK
        return Pair.of(TokenOperationResult.OK, token);
    }

    @Transactional
    public PasswordResetToken generatePasswordResetToken(final User user) throws OperationNotSuccessfulException {
        assert(user != null);
        assert(user.getId() != null);

        if (user.getStatus() != UserStatus.ACTIVE) {
            log.debug("Token generation not possible, because user {} is in state {}.",
                      () -> user.getId(), () -> user.getStatus().toString());
            throw new IllegalArgumentException("A password reset token may only be generated for ACTIVE users. "
                                               + "But User " + user.getId() + " has status " + user.getStatus() + ".");
        }

        return generateSecurityToken(user, applicationProperties.getPasswordTokenExpirationTime(),
                                           tokenRepository);
    }

    @Transactional
    public User getUserForToken(String tokenValue) {
        Objects.nonNull(tokenValue);

        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenValue(tokenValue);

        if (tokenOpt.isPresent()) {
            return tokenOpt.get().getUser();
        } else {
            return null;
        }
    }

    @Transactional
    public TokenOperationResult resetPassword(String tokenValue, String newPassword) {
        if (tokenValue == null) return TokenOperationResult.INVALID_TOKEN;
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenValue(tokenValue);

        if (!tokenOpt.isPresent()) return TokenOperationResult.INVALID_TOKEN;
        PasswordResetToken token = tokenOpt.get();

        if (token.getExpirationTime().isBefore(LocalDateTime.now())) {
            return TokenOperationResult.TOKEN_EXPIRED;
        }

        User tokenOwner = token.getUser();
        tokenOwner.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(tokenOwner);

        tokenRepository.delete(token);

        return TokenOperationResult.OK;
    }

    protected PasswordResetToken createToken() {
        return new PasswordResetToken();
    }
}