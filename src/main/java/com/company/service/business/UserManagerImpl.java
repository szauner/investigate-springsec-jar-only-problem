package com.company.service.business;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.service.model.User;
import com.company.service.persistence.UserRepository;

@Service
public class UserManagerImpl implements UserManager {

    @Autowired
    private PasswordEncoder passwordEncoder;
	@Autowired
	private UserRepository userRepository;

	@Transactional
    public void saveUser(User user) {
	    Objects.requireNonNull(user);
        userRepository.save(user);
    }

	@Transactional(readOnly=true)
	public Optional<User> getUser(Long userId) {
	    if (userId == null) return Optional.ofNullable(null);
		return userRepository.findById(userId);
	}

	@Transactional(readOnly=true)
    public Optional<User> getUser(String email) {
	    if (email == null) return Optional.ofNullable(null);
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly=true)
    public Optional<User> getUserByNickname(String username) {
        if (username == null) return Optional.ofNullable(null);
        return userRepository.findByNickname(username);
    }

    @Transactional(readOnly=true)
    public boolean checkPassword(Long userId, String password) {
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            return passwordEncoder.matches(password, user.get().getPassword());
        } else {
            return false;
        }
    }
}