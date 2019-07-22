package com.company.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class UserDetailsDTO extends AbstractModelObject {
    private Long id;
    private String nickname;

    public UserDetailsDTO() {
    }

    public UserDetailsDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String username) {
        this.nickname = (username == null) ? null : username.trim();
    }
}