package com.techflow.techhubbackend.model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class RegularUserModelTest {

    @Test
    void testRegularUserModelMapConstructor() {
        String username = "user";
        String type = "merchant";
        int totalPoints = 100;

        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("type", type);
        map.put("totalPoints", totalPoints);

        RegularUserModel regularUser = new RegularUserModel(map);

        assertEquals(username, regularUser.getUsername());
        assertEquals("no password", regularUser.getPassword());
        assertEquals(type, regularUser.getType());
        assertEquals("no profile picture", regularUser.getProfilePicture());
        assertEquals("no account_status", regularUser.getAccountStatus());
        assertEquals(100, regularUser.getTotalPoints());
        assertEquals(0, regularUser.getCurrentPoints());
        assertFalse(regularUser.isVipStatus());
    }

    @Test
    void testEqualsMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        String differentUsername = "username";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";
        int totalPoints = 10;
        int differentTotalPoints = 0;
        int currentPoints = 10;
        boolean vipStatus = false;

        RegularUserModel regularUser = new RegularUserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);

        // Check object of different class
        Object obj = new Object();
        assertNotEquals(true, regularUser.equals(obj));

        // Check same object
        assertEquals(true, regularUser.equals(regularUser));

        // Check object with same field values
        RegularUserModel regularUser2 = new RegularUserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);
        assertEquals(true, regularUser.equals(regularUser2));

        // Check object with different superclass field values
        regularUser2.setUsername(differentUsername);
        assertNotEquals(true, regularUser.equals(regularUser2));

        // Check object with different field values
        regularUser2.setUsername(username);
        regularUser2.setTotalPoints(differentTotalPoints);
        assertNotEquals(true, regularUser.equals(regularUser2));

        System.out.println(regularUser.toString());
    }

    @Test
    void testGetMapMethod() {
        String email = "email";
        String password = "pass";
        String username = "user";
        String type = " type";
        String profilePicture = "profile";
        String accountStatus = "account";
        int totalPoints = 10;
        int currentPoints = 10;
        boolean vipStatus = false;

        RegularUserModel regularUser = new RegularUserModel(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);

        Map<String, Object> map = regularUser.getMap();

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
