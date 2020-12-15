package com.techflow.techhubbackend.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserModel {

    protected String email;
    protected String password;
    protected String username;
    protected UserType type;
    protected String profilePicture;
    protected String accountStatus;
    private int totalPoints;
    private int currentPoints;
    private boolean vipStatus;

    public UserModel() {
    }

    public UserModel(String email, String password, String username, UserType type, String profilePicture, String accountStatus) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.type = type;
        this.profilePicture = profilePicture;
        this.accountStatus = accountStatus;
    }

    public UserModel(String email, String password, String username, UserType type, String profilePicture,
                     String accountStatus, int totalPoints, int currentPoints, boolean vipStatus) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.type = type;
        this.profilePicture = profilePicture;
        this.accountStatus = accountStatus;
        if (type == UserType.REGULAR_USER) {
            this.totalPoints = totalPoints;
            this.currentPoints = currentPoints;
            this.vipStatus = vipStatus;
        }
    }

    public UserModel(UserModel userModel) {
        this.email = userModel.getEmail();
        this.password = userModel.getPassword();
        this.username = userModel.getUsername();
        this.type = userModel.getType();
        this.profilePicture = userModel.getProfilePicture();
        this.accountStatus = userModel.getAccountStatus();
        if (type == UserType.REGULAR_USER) {
            this.totalPoints = userModel.getTotalPoints();
            this.currentPoints = userModel.getCurrentPoints();
            this.vipStatus = userModel.isVipStatus();
        }
    }

    public UserModel(Map<String, Object> map) {
        this.email = (String) map.getOrDefault("email", "no email");
        this.password = (String) map.getOrDefault("password", "no password");
        this.username = (String) map.getOrDefault("username", "no username");
        this.type = UserType.valueOf(map.getOrDefault("type", UserType.NO_TYPE).toString());
        this.profilePicture = (String) map.getOrDefault("profilePicture", "no profile picture");
        this.accountStatus = (String) map.getOrDefault("accountStatus", "no account_status");
        if (type == UserType.REGULAR_USER) {
            this.totalPoints = (Integer) map.getOrDefault("totalPoints", 0);
            this.currentPoints = (Integer) map.getOrDefault("currentPoints", 0);
            this.vipStatus = (Boolean) map.getOrDefault("vipStatus", false);
        }
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public UserType getType() {
        return type;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setType(UserType type) {
        this.type = type;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }

    public int getCurrentPoints() {
        return currentPoints;
    }

    public void setCurrentPoints(int currentPoints) {
        this.currentPoints = currentPoints;
    }

    public boolean isVipStatus() {
        return vipStatus;
    }

    public void setVipStatus(boolean vipStatus) {
        this.vipStatus = vipStatus;
    }

    @Override
    public String toString() {
        String s = "UserModel{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", accountStatus='" + accountStatus + '\'';

        if (type == UserType.REGULAR_USER)
            s += ", totalPoints=" + totalPoints +
                    ", currentPoints=" + currentPoints +
                    ", vipStatus=" + vipStatus;

        s += "}";
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;

        boolean eq = Objects.equals(email, userModel.email) &&
                Objects.equals(password, userModel.password) &&
                Objects.equals(username, userModel.username) &&
                Objects.equals(type, userModel.type) &&
                Objects.equals(profilePicture, userModel.profilePicture) &&
                Objects.equals(accountStatus, userModel.accountStatus);

        if (type == UserType.REGULAR_USER) {
            eq = eq && totalPoints == userModel.totalPoints &&
                    currentPoints == userModel.currentPoints &&
                    vipStatus == userModel.vipStatus;
        }

        return eq;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, username, type, profilePicture, accountStatus, totalPoints, currentPoints, vipStatus);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("email", email);
        map.put("password", password);
        map.put("username", username);
        map.put("type", type);
        map.put("profilePicture", profilePicture);
        map.put("accountStatus", accountStatus);

        if (type == UserType.REGULAR_USER) {
            map.put("totalPoints", totalPoints);
            map.put("currentPoints", currentPoints);
            map.put("vipStatus", vipStatus);
        }

        return map;
    }

    public Map<String, Object> getMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (email != null || includeEmptyFields)
            map.put("email", email);
        if (password != null || includeEmptyFields)
            map.put("password", password);
        if (username != null || includeEmptyFields)
            map.put("username", username);
        if (type != null || includeEmptyFields)
            map.put("type", type);
        if (profilePicture != null || includeEmptyFields)
            map.put("profilePicture", profilePicture);
        if (accountStatus != null || includeEmptyFields)
            map.put("accountStatus", accountStatus);

        if (type == UserType.REGULAR_USER) {
            map.put("totalPoints", totalPoints);
            map.put("currentPoints", currentPoints);
            map.put("vipStatus", vipStatus);
        }

        return map;
    }
}
