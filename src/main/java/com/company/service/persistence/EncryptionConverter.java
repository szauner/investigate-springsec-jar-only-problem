package com.company.service.persistence;

import javax.persistence.AttributeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;

public class EncryptionConverter implements AttributeConverter<String, String> {
    private final static Logger log = LogManager.getLogger(EncryptionConverter.class);

    @Autowired
    private PooledPBEStringEncryptor strongEncryptor;

    public String convertToDatabaseColumn(String value) {
        log.trace("Encrypting: {}", value);
        String encryptedValue = strongEncryptor.encrypt(value);
        log.trace("Encrypted result: {}", encryptedValue);
        return encryptedValue;
    }

    public String convertToEntityAttribute(String dbValue) {
        log.trace("Encrypting: {}", dbValue);
        String decryptedValue = strongEncryptor.decrypt(dbValue);
        log.trace("Decrypted result: {}", decryptedValue);
        return decryptedValue;
    }
}