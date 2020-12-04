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
        assertEquals("no profile picture", user.getProfilePictureId());
        assertEquals("no account_status", user.getAccountStatus());
    }

    @Test
    void testEqualsMethod() {
        String username = "user";
        String differentUsername = "username";
        String password = "pass";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";

        UserModel user = new UserModel(username, password, type, profilePicture, accountStatus);

        // Check object of different class
        Object obj = new Object();
        assertNotEquals(true, user.equals(obj));

        // Check same object
        assertEquals(true, user.equals(user));

        // Check object with same field values
        UserModel user2 = new UserModel(username, password, type, profilePicture, accountStatus);
        assertEquals(true, user.equals(user2));

        // Check object with different field values
        user2.setUsername(differentUsername);
        assertNotEquals(true, user.equals(user2));
    }

    @Test
    void testGetMapMethod() {
        String username = "user";
        String password = "pass";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";

        UserModel user = new UserModel(username, password, type, profilePicture, accountStatus);

        Map<String, Object> map = user.getMap();

        assertEquals(username, map.get("username"));
        assertEquals(password, map.get("password_hash"));
        assertEquals(type, map.get("type"));
        assertEquals(profilePicture, map.get("profile_picture"));
        assertEquals(accountStatus, map.get("account_status"));
    }
}
