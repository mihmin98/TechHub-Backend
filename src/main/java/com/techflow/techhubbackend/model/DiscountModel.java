package com.techflow.techhubbackend.model;

import java.util.*;

public class DiscountModel {

    private String id;
    private String sellerEmail;
    private String title;
    private String description;
    private List<String> pictures;
    private Integer pointsCost;
    private Boolean vipStatus;
    private Boolean isActive;

    public DiscountModel() {
    }

    public DiscountModel(String id, String sellerEmail, String title, String description, List<String> pictures, Integer pointsCost, Boolean vipStatus, Boolean isActive) {
        this.id = id;
        this.sellerEmail = sellerEmail;
        this.title = title;
        this.description = description;
        this.pictures = pictures;
        this.pointsCost = pointsCost;
        this.vipStatus = vipStatus;
        this.isActive = isActive;
    }

    public DiscountModel(DiscountModel discountModel) {
        this.id = discountModel.id;
        this.sellerEmail = discountModel.sellerEmail;
        this.title = discountModel.title;
        this.description = discountModel.description;
        this.pictures = discountModel.pictures;
        this.pointsCost = discountModel.pointsCost;
        this.vipStatus = discountModel.vipStatus;
        this.isActive = discountModel.isActive;
    }

    public DiscountModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.sellerEmail = (String) map.getOrDefault("sellerEmail", "no seller email");
        this.title = (String) map.getOrDefault("title", "no title");
        this.description = (String) map.getOrDefault("description", "no seller description");
        this.pictures = (List<String>) map.getOrDefault("pictures", new ArrayList<String>());
        this.pointsCost = ((Long) map.getOrDefault("pointsCost", 0)).intValue();
        this.vipStatus = (Boolean) map.getOrDefault("vipStatus", false);
        this.isActive = (Boolean) map.getOrDefault("isActive", true);
    }

    public String getId() {
        return id;
    }

    public String getSellerEmail() {
        return sellerEmail;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getPictures() {
        return pictures;
    }

    public Integer getPointsCost() {
        return pointsCost;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSellerEmail(String sellerEmail) {
        this.sellerEmail = sellerEmail;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPictures(ArrayList<String> pictures) {
        this.pictures = pictures;
    }

    public void setPointsCost(Integer pointsCost) {
        this.pointsCost = pointsCost;
    }

    public Boolean getVipStatus() {
        return vipStatus;
    }

    public void setVipStatus(Boolean vipStatus) {
        this.vipStatus = vipStatus;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "DiscountModel{" +
                "id='" + id + '\'' +
                ", sellerEmail='" + sellerEmail + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", pictures=" + pictures +
                ", pointsCost=" + pointsCost +
                ", vipStatus=" + vipStatus +
                ", isActive=" + isActive +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountModel that = (DiscountModel) o;
        return pointsCost == that.pointsCost &&
                Objects.equals(id, that.id) &&
                Objects.equals(sellerEmail, that.sellerEmail) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(pictures, that.pictures) &&
                Objects.equals(vipStatus, that.vipStatus) &&
                Objects.equals(isActive, that.isActive);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sellerEmail, title, description, pictures, pointsCost, vipStatus, isActive);
    }

    public Map<String, Object> generateMap() {
        Map<java.lang.String, java.lang.Object> map = new HashMap<>();

        map.put("id", id);
        map.put("sellerEmail", sellerEmail);
        map.put("title", title);
        map.put("description", description);
        if (pictures != null) {
            map.put("pictures", pictures);
        }
        map.put("pointsCost", pointsCost);
        map.put("vipStatus", vipStatus);
        map.put("isActive", isActive);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (sellerEmail != null || includeEmptyFields)
            map.put("sellerEmail", sellerEmail);
        if (title != null || includeEmptyFields)
            map.put("title", title);
        if (description != null || includeEmptyFields)
            map.put("description", description);
        if (pictures != null || includeEmptyFields)
            map.put("pictures", pictures);
        if (pointsCost != null || includeEmptyFields)
            map.put("pointsCost", pointsCost);
        if (vipStatus != null || includeEmptyFields)
            map.put("vipStatus", vipStatus);
        if (isActive != null || includeEmptyFields)
            map.put("isActive", isActive);

        return map;
    }
}
