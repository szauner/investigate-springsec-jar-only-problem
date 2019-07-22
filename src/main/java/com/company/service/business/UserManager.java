package com.company.service.business;

import java.util.Optional;

import com.company.service.model.User;

public interface UserManager {
    public void saveUser(User user);

	public Optional<User> getUser(Long userId);

	public Optional<User> getUser(String email);

	public Optional<User> getUserByNickname(String username);

	public boolean checkPassword(Long userId, String password);
}
