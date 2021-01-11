package com.techflow.techhubbackend.model;

import java.util.*;

public class DiscountModel {

    private String id;
    private String sellerEmail;
    private String title;
    private String description;
    private ArrayList<String> pictures;
    private int pointsCost;

    public DiscountModel() {
    }

    public DiscountModel(String id, String sellerEmail, String title, String description, ArrayList<String> pictures, int pointsCost) {
        this.id = id;
        this.sellerEmail = sellerEmail;
        this.title = title;
        this.description = description;
        this.pictures = pictures;
        this.pointsCost = pointsCost;
    }

    public DiscountModel(DiscountModel discountModel) {
        this.id = discountModel.id;
        this.sellerEmail = discountModel.sellerEmail;
        this.title = discountModel.title;
        this.description = discountModel.description;
        this.pictures = discountModel.pictures;
        this.pointsCost = discountModel.pointsCost;
    }

    public DiscountModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.sellerEmail = (String) map.getOrDefault("sellerEmail", "no seller email");
        this.title = (String) map.getOrDefault("title", "no title");
        this.description = (String) map.getOrDefault("description", "no seller description");
        this.pictures = (ArrayList<String>) map.getOrDefault("pictures", new ArrayList<String>());
        this.pointsCost = ((Long) map.getOrDefault("pointsCost", 0)).intValue();
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

    public ArrayList<String> getPictures() {
        return pictures;
    }

    public int getPointsCost() {
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

    public void setPointsCost(int pointsCost) {
        this.pointsCost = pointsCost;
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
                Objects.equals(pictures, that.pictures);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sellerEmail, title, description, pictures, pointsCost);
    }

    public Map<String, Object> generateMap() {
        Map<java.lang.String, java.lang.Object> map = new HashMap<>();

        map.put("id", id);
        map.put("sellerEmail", sellerEmail);
        map.put("title", title);
        map.put("description", description);
        if (pictures != null) {
            map.put("pictures", Arrays.asList(pictures.toArray()));
        }
        map.put("pointsCost", pointsCost);

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
        if (pictures != null || includeEmptyFields) {
            map.put("pictures", Arrays.asList(pictures.toArray()));
        }
        map.put("pointsCost", pointsCost);

        return map;
    }
}
