package com.company.service.business;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.company.service.model.User;
import com.company.service.model.UserStatus;
import com.company.service.persistence.UserRepository;
import com.company.service.security.ApplicationRoleGrantedAuthority;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(@NotEmpty String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getStatus() == UserStatus.ACTIVE,
                true, // Account not expired
                true, // Credentials not expired
                user.getStatus() != UserStatus.LOCKED,
                getAuthorities(user));
        } else {
            throw new UsernameNotFoundException("No user having the e-mail " + email + " could be found.");
        }
    }

    /*
     * Retrieves the authorities for a given user.
     */
    private  Collection<? extends GrantedAuthority> getAuthorities(User user) {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

        // First authority is the application role itself
        String applicationRoleName = user.getRole().name();
        authorities.add(new ApplicationRoleGrantedAuthority("ROLE_" + applicationRoleName));
        authorities.add(new SimpleGrantedAuthority("READ_DATA"));
        authorities.add(new SimpleGrantedAuthority("WRITE_DATA"));

        return authorities;
    }
}