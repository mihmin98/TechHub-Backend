package com.techflow.techhubbackend.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UserModel {

    protected String username;
    protected String password;
    protected String type;
    protected String profilePicture;
    protected String accountStatus;

    public UserModel() {
    }

    public UserModel(String username, String password, String type, String profilePicture, String accountStatus) {
        this.username = username;
        this.password = password;
        this.type = type;
        this.profilePicture = profilePicture;
        this.accountStatus = accountStatus;
    }

    public UserModel(Map<String, Object> map) {
        this.username = (String) map.getOrDefault("username", "no username");
        this.password = (String) map.getOrDefault("password_hash", "no password");
        this.type = (String) map.getOrDefault("type", "no type");
        this.profilePicture = (String) map.getOrDefault("profile_picture", "no profile picture");
        this.accountStatus = (String) map.getOrDefault("account_status", "no account_status");
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProfilePictureId() {
        return profilePicture;
    }

    public void setProfilePictureId(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getAccountStatus() {
        return accountStatus;
    }

    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    @Override
    public String toString() {
        return "UserModel{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", type='" + type + '\'' +
                ", profilePicture=" + profilePicture + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserModel userModel = (UserModel) o;
        return profilePicture.equals(userModel.profilePicture) &&
                Objects.equals(username, userModel.username) &&
                Objects.equals(password, userModel.password) &&
                Objects.equals(type, userModel.type) &&
                Objects.equals(accountStatus, userModel.accountStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password, type, profilePicture, accountStatus);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("username", username);
        map.put("password_hash", password);
        map.put("type", type);
        map.put("profile_picture", profilePicture);
        map.put("account_status", accountStatus);
        return map;
    }
}
