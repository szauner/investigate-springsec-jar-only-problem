package com.company.service.testdata;

import com.company.service.model.User;
import com.company.service.model.UserStatus;
import com.company.service.security.ApplicationRole;

public class UserTestDataProvider {
    private final static String COMMON_USER_PASSWORD_PLAIN = "Company2000!";
    private final static String COMMON_USER_PASSWORD_ENCRYPTED
        = "$2a$10$QLfNHWFW0xinGbI2RavHXe8TBdCxCb0Wp7FNzHHAnM8Q1Rmjw5xGG";

    public static User getAdminUser() {
        User testUser = new User();
        testUser.setEmail("active.admin@company.com");
        testUser.setPassword(COMMON_USER_PASSWORD_ENCRYPTED);
        testUser.setRole(ApplicationRole.ADMIN);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setNickname("Saruman");
        return testUser;
    }


    public static User getLockedAdminUser() {
        User testUser = new User();
        testUser.setEmail("locked.admin@company.com");
        testUser.setPassword(COMMON_USER_PASSWORD_ENCRYPTED);
        testUser.setRole(ApplicationRole.ADMIN);
        testUser.setStatus(UserStatus.LOCKED);
        testUser.setNickname("locko");
        return testUser;
    }


    public static User getActiveStandardUser() {
        User testUser = new User();
        testUser.setEmail("active.user@company.com");
        testUser.setPassword(COMMON_USER_PASSWORD_ENCRYPTED);
        testUser.setRole(ApplicationRole.STANDARD);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setNickname("User1234");
        return testUser;
    }


    public static String getUserPasswordInPlainText() {
        return COMMON_USER_PASSWORD_PLAIN;
    }
}