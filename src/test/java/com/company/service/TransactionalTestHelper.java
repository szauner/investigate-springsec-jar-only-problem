package com.company.service;

import java.util.Optional;

import com.company.service.model.PasswordResetToken;
import com.company.service.model.User;

public interface TransactionalTestHelper {

    public void deleteUser(User user);

    public void savePasswordResetToken(PasswordResetToken token);

    public Optional<PasswordResetToken> getPasswordResetToken(User user);

    public void removePasswordResetToken(PasswordResetToken token);
}
