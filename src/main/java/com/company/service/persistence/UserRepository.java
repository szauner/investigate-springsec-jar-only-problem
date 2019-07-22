package com.company.service.persistence;

import java.util.Optional;

import com.company.service.model.User;

public interface UserRepository extends BaseRepository<User, Long> {

    public Optional<User> findByEmail(String email);

    public Optional<User> findByNickname(String username);
}