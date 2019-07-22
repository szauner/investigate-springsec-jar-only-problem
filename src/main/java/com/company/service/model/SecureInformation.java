package com.company.service.model;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.company.service.persistence.EncryptionConverter;

@Entity
@Table(name = "`T_Secure`", schema = "public")
public class SecureInformation {
    @Id
    @Column(name = "entry_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "value")
    @Convert(converter = EncryptionConverter.class)
    private String value;

    @Column(name = "value", insertable = false, updatable = false)
    private String valueEncrypted;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValueEncrypted() {
        return valueEncrypted;
    }

    public void setValueEncrypted(String valueEncrypted) {
        this.valueEncrypted = valueEncrypted;
    }
}
