package com.company.service;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import com.company.service.model.PasswordResetToken;
import com.company.service.model.User;
import com.company.service.persistence.PasswordResetTokenRepository;
import com.company.service.persistence.UserRepository;

public class TransactionalTestHelperImpl implements TransactionalTestHelper {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordResetTokenRepository passwordTokenRepository;

    @Transactional
    public void deleteUser(User user) {
        userRepository.delete(user);
    }

    @Transactional
    public void savePasswordResetToken(PasswordResetToken token) {
        passwordTokenRepository.save(token);
    }

    @Transactional
    public Optional<PasswordResetToken> getPasswordResetToken(User user) {
        return passwordTokenRepository.findByUserId(user.getId());
    }

    @Transactional
    public void removePasswordResetToken(PasswordResetToken token) {
        passwordTokenRepository.delete(token);
    }
}
