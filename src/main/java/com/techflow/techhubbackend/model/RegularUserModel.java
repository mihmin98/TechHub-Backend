package com.techflow.techhubbackend.model;

import java.util.Map;
import java.util.Objects;

public class RegularUserModel extends UserModel {

    private int totalPoints;
    private int currentPoints;
    private boolean vipStatus;

    public RegularUserModel() {
        super();
    }

    public RegularUserModel(String email, String password, String username, String type, String profilePicture,
                            String accountStatus, int totalPoints, int currentPoints, boolean vipStatus) {
        super(email, password, username, type, profilePicture, accountStatus);
        this.totalPoints = totalPoints;
        this.currentPoints = currentPoints;
        this.vipStatus = vipStatus;
    }

    public RegularUserModel(Map<String, Object> map) {
        super(map);
        this.totalPoints = (Integer) map.getOrDefault("total_points", 0);
        this.currentPoints = (Integer) map.getOrDefault("current_points", 0);
        this.vipStatus = (Boolean) map.getOrDefault("vip_status", false);
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
        return "RegularUserModel{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", username='" + username + '\'' +
                ", type='" + type + '\'' +
                ", profilePicture=" + profilePicture + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", totalPoints=" + totalPoints +
                ", currentPoints=" + currentPoints +
                ", vipStatus=" + vipStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegularUserModel that = (RegularUserModel) o;
        return super.equals(o) &&
                totalPoints == that.totalPoints &&
                currentPoints == that.currentPoints &&
                vipStatus == that.vipStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, password, username, type, profilePicture,
                accountStatus, totalPoints, currentPoints, vipStatus);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = super.getMap();

        map.put("total_points", totalPoints);
        map.put("current_points", currentPoints);
        map.put("vip_status", vipStatus);
        return map;
    }
}
