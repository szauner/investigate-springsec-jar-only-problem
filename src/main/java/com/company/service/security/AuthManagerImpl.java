package com.company.service.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.company.service.business.UserManager;
import com.company.service.model.User;

@Service
public class AuthManagerImpl implements AuthManager {
    private final static Logger log = LogManager.getLogger(AuthManagerImpl.class);

    @Autowired
    private UserManager userManager;
    @Autowired
    private AuthenticationFacade authenticationFacade;

    public boolean checkAuthenticationStatus(Long userId) {
        Object principal = authenticationFacade.getAuthentication().getPrincipal();
        User user = userManager.getUser(principal.toString()).orElse(new User());
        log.debug("Checking authentication status for {}: The current principal is {}", userId, principal);
        return (userId != null) && (userId.equals(user.getId()));
    }

    public Long getAuthenticatedUsersId() {
        Object principal = authenticationFacade.getAuthentication().getPrincipal();
        User user = userManager.getUser(principal.toString()).orElse(new User());
        return user.getId();
    }

    public UserPrincipal getAuthenticatedPrincipal(boolean allowAnonymous) throws NotAuthenticatedException {
        Authentication auth = authenticationFacade.getAuthentication();

        if (auth == null) {
            throw new NotAuthenticatedException();
        }

        Object principal = auth.getPrincipal();
        UserPrincipal userPrincipal;
        if (principal instanceof UserPrincipal) {
            userPrincipal = (UserPrincipal)principal;
        } else if (principal instanceof String || "anonymousUser".equals(principal)) {
            userPrincipal = new UserPrincipal();
            userPrincipal.setApplicationRole(ApplicationRole.ANONYMOUS);
        } else {
            log.error("Unexpected principal type {}; principal is {}.",
                      (principal == null ? null : principal.getClass()), principal);
            throw new NotAuthenticatedException();
        }

        if (userPrincipal.getApplicationRole() != ApplicationRole.ANONYMOUS
                && userPrincipal.getUserId() == null) {
            log.warn("Encountered non-anonymous user-principal without ID: {}. This might be an attempt to manipulate "
                + "the access token.", principal);
            throw new NotAuthenticatedException();
        }

        if (userPrincipal.getApplicationRole() == ApplicationRole.ANONYMOUS && !allowAnonymous) {
            log.debug("Anynoumous access is forbidden.");
            throw new NotAuthenticatedException();
        }

        return userPrincipal;
    }
}