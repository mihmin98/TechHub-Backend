package com.techflow.techhubbackend.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserModel {

    protected String email;
    protected String password;
    protected String username;
    protected String type;
    protected String profilePicture;
    protected String accountStatus;

    public UserModel() {
    }

    public UserModel(String email, String password, String username, String type, String profilePicture, String accountStatus) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.type = type;
        this.profilePicture = profilePicture;
        this.accountStatus = accountStatus;
    }

    public UserModel(Map<String, Object> map) {
        this.email = (String) map.getOrDefault("email", "no email");
        this.password = (String) map.getOrDefault("password_hash", "no password");
        this.username = (String) map.getOrDefault("username", "no username");
        this.type = (String) map.getOrDefault("type", "no type");
        this.profilePicture = (String) map.getOrDefault("profile_picture", "no profile picture");
        this.accountStatus = (String) map.getOrDefault("account_status", "no account_status");
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

    public String getType() {
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

    public void setType(String type) {
        this.type = type;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return Objects.equals(email, userModel.email) &&
                Objects.equals(password, userModel.password) &&
                Objects.equals(username, userModel.username) &&
                Objects.equals(type, userModel.type) &&
                Objects.equals(profilePicture, userModel.profilePicture) &&
                Objects.equals(accountStatus, userModel.accountStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, username, type, profilePicture, accountStatus);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("email", email);
        map.put("password_hash", password);
        map.put("username", username);
        map.put("type", type);
        map.put("profile_picture", profilePicture);
        map.put("account_status", accountStatus);
        return map;
    }
}
