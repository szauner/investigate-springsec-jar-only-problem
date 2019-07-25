package com.company.service.business;

import com.company.service.model.PasswordResetToken;
import com.company.service.model.User;
import com.company.service.util.OperationNotSuccessfulException;

public interface PasswordResetManager {
    public PasswordResetToken generatePasswordResetToken(final User user) throws OperationNotSuccessfulException;
}