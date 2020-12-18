package com.techflow.techhubbackend.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class UserModelTest {

    @Test
    void testUserModelMapConstructor() {
        String username = "user";
        UserType type = UserType.REGULAR_USER;

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
    void testRegularUserModelMapConstructor() {
        String username = "user";
        UserType type = UserType.REGULAR_USER;
        int totalPoints = 100;

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("type", type);
        map.put("totalPoints", totalPoints);

        UserModel user = new UserModel(map);

        assertEquals(username, user.getUsername());
        assertEquals("no password", user.getPassword());
        assertEquals(type, user.getType());
        assertEquals("no profile picture", user.getProfilePicture());
        assertEquals("no account_status", user.getAccountStatus());
        assertEquals(totalPoints, user.getTotalPoints());
        assertEquals(0, user.getCurrentPoints());
        assertFalse(user.isVipStatus());
    }

    @Test
    void testEqualsMethod() {
        String email = "email";
        String username = "user";
        String differentUsername = "username";
        String password = "pass";
        UserType type = UserType.REGULAR_USER;
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
    void testRegularUserEqualsMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        String differentUsername = "username";
        UserType type = UserType.REGULAR_USER;
        String profilePicture = "profile";
        String accountStatus = "account";
        int totalPoints = 10;
        int differentTotalPoints = 0;
        int currentPoints = 10;
        boolean vipStatus = false;

        UserModel user = new UserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);

        // Check object of different class
        Object obj = new Object();
        assertNotEquals(true, user.equals(obj));

        // Check same object
        assertEquals(true, user.equals(user));

        // Check object with same field values
        UserModel regularUser2 = new UserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);
        assertEquals(true, user.equals(regularUser2));

        // Check object with different superclass field values
        regularUser2.setUsername(differentUsername);
        assertNotEquals(true, user.equals(regularUser2));

        // Check object with different field values
        regularUser2.setUsername(username);
        regularUser2.setTotalPoints(differentTotalPoints);
        assertNotEquals(true, user.equals(regularUser2));
    }

    @Test
    void testGetMapMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        UserType type = UserType.REGULAR_USER;
        String profilePicture = "profile";
        String accountStatus = "account";

        UserModel user = new UserModel(email, password, username, type, profilePicture, accountStatus);

        Map<String, Object> map = user.generateMap();

        assertEquals(email, map.get("email"));
        assertEquals(password, map.get("password"));
        assertEquals(username, map.get("username"));
        assertEquals(type, map.get("type"));
        assertEquals(profilePicture, map.get("profilePicture"));
        assertEquals(accountStatus, map.get("accountStatus"));
    }

    @Test
    void testRegularUserGetMapMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        UserType type = UserType.REGULAR_USER;
        String profilePicture = "profile";
        String accountStatus = "account";
        int totalPoints = 10;
        int currentPoints = 10;
        boolean vipStatus = false;

        UserModel user = new UserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);

        Map<String, Object> map = user.generateMap();

        assertEquals(email, map.get("email"));
        assertEquals(password, map.get("password"));
        assertEquals(username, map.get("username"));
        assertEquals(type, map.get("type"));
        assertEquals(profilePicture, map.get("profilePicture"));
        assertEquals(accountStatus, map.get("accountStatus"));
        assertEquals(totalPoints, map.get("totalPoints"));
        assertEquals(currentPoints, map.get("currentPoints"));
        assertEquals(vipStatus, map.get("vipStatus"));
    }
}
