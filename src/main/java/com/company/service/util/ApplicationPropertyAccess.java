package com.company.service.util;

import org.springframework.beans.factory.annotation.Value;

public class ApplicationPropertyAccess {
    @Value("${service.verification.expiration}")
    private long verificationTokenExpirationTime;

    @Value("${service.password.reset.expiration}")
    private long passwordResetTokenExpirationTime;

    @Value("${service.authentication.access.expiration}")
    private int oauth2AccessTokenExpirationTime;

    @Value("${service.authentication.refresh.expiration}")
    private int oauth2RefreshTokenExpirationTime;

    @Value("${logging.jersey.loggername}")
    private String jerseyLoggerName;

    @Value("${logging.jersey.maxentitysize}")
    private Integer jerseyMaxEntitySize;

    @Value("${service.encryptor.algorithm}")
    private String encryptorAlgorithm;

    @Value("${service.encryptor.poolsize}")
    private Integer encryptorPoolSize;

    @Value("${service.encryptor.password}")
    private String encryptorPassword;

    public long getVerificationTokenExpirationTime() {
        return verificationTokenExpirationTime;
    }

    public long getPasswordTokenExpirationTime() {
        return passwordResetTokenExpirationTime;
    }

    public int getOauth2AccessTokenExpirationTime() {
        return oauth2AccessTokenExpirationTime;
    }

    public int getOauth2RefreshTokenExpirationTime() {
        return oauth2RefreshTokenExpirationTime;
    }

    public long getPasswordResetTokenExpirationTime() {
        return passwordResetTokenExpirationTime;
    }

    public String getJerseyLoggerName() {
        return jerseyLoggerName;
    }

    public Integer getJerseyMaxEntitySize() {
        return jerseyMaxEntitySize;
    }

    public String getEncryptorAlgorithm() {
        return encryptorAlgorithm;
    }

    public Integer getEncryptorPoolSize() {
        return encryptorPoolSize;
    }

    public String getEncryptorPassword() {
        return encryptorPassword;
    }
}
