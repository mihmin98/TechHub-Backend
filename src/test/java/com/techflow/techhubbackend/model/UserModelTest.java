package com.techflow.techhubbackend.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class UserModelTest {

    @Test
    void testUserModelMapConstructor() {
        String username = "user";
        String type = "merchant";

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("type", type);

        UserModel user = new UserModel(map);

        assertEquals(username, user.getUsername());
        assertEquals("no password", user.getPassword());
        assertEquals(type, user.getType());
        assertEquals("no profile picture", user.getProfilePicture());
        assertEquals("no account_status", user.getAccountStatus());
    }

    @Test
    void testEqualsMethod() {
        String email = "email";
        String username = "user";
        String differentUsername = "username";
        String password = "pass";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";

        UserModel user = new UserModel(email, password, username, type, profilePicture, accountStatus);

        // Check object of different class
        Object obj = new Object();
        assertNotEquals(true, user.equals(obj));

        // Check same object
        assertEquals(true, user.equals(user));

        // Check object with same field values
        UserModel user2 = new UserModel(email, password, username, type, profilePicture, accountStatus);
        assertEquals(true, user.equals(user2));

        // Check object with different field values
        user2.setUsername(differentUsername);
        assertNotEquals(true, user.equals(user2));
    }

    @Test
    void testGetMapMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";

        UserModel user = new UserModel(email, password, username, type, profilePicture, accountStatus);

        Map<String, Object> map = user.getMap();

        assertEquals(email, map.get("email"));
        assertEquals(password, map.get("password"));
        assertEquals(username, map.get("username"));
        assertEquals(type, map.get("type"));
        assertEquals(profilePicture, map.get("profilePicture"));
        assertEquals(accountStatus, map.get("accountStatus"));
    }
}
