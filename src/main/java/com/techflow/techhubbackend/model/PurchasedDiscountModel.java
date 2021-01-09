package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PurchasedDiscountModel {

    private String id;
    private String purchaserEmail;
    private int pointsSpent;
    private String discoutId;
    private Date datePurchased;

    public PurchasedDiscountModel() {
    }

    public PurchasedDiscountModel(String id, String purchaserEmail, int pointsSpent, String discoutId, Date datePurchased) {
        this.id = id;
        this.purchaserEmail = purchaserEmail;
        this.pointsSpent = pointsSpent;
        this.discoutId = discoutId;
        this.datePurchased = datePurchased;
    }

    public PurchasedDiscountModel(PurchasedDiscountModel purchasedDiscountModel) {
        this.id = purchasedDiscountModel.id;
        this.purchaserEmail = purchasedDiscountModel.purchaserEmail;
        this.pointsSpent = purchasedDiscountModel.pointsSpent;
        this.discoutId = purchasedDiscountModel.discoutId;
        this.datePurchased = purchasedDiscountModel.datePurchased;
    }

    public PurchasedDiscountModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.purchaserEmail = (String) map.getOrDefault("purchaserEmail", "no purchaser email");
        this.pointsSpent = (Integer) map.getOrDefault("pointsSpent", 0);
        this.discoutId = (String) map.getOrDefault("discountId", "no discount id");
    }

    public String getId() {
        return id;
    }

    public String getPurchaserEmail() {
        return purchaserEmail;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public String getDiscoutId() {
        return discoutId;
    }

    public Date getDatePurchased() {
        return datePurchased;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPurchaserEmail(String purchaserEmail) {
        this.purchaserEmail = purchaserEmail;
    }

    public void setPointsSpent(int pointsSpent) {
        this.pointsSpent = pointsSpent;
    }

    public void setDiscoutId(String discoutId) {
        this.discoutId = discoutId;
    }

    public void setDatePurchased(Date datePurchased) {
        this.datePurchased = datePurchased;
    }

    public PurchasedDiscountModel builderSetDatePurchased(Timestamp datePurchased) {
        this.datePurchased = datePurchased.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "PurchasedDiscountModel{" +
                "id='" + id + '\'' +
                ", purchaserEmail='" + purchaserEmail + '\'' +
                ", pointsSpent=" + pointsSpent +
                ", discoutId='" + discoutId + '\'' +
                ", datePurchased=" + datePurchased +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchasedDiscountModel that = (PurchasedDiscountModel) o;
        return pointsSpent == that.pointsSpent &&
                Objects.equals(id, that.id) &&
                Objects.equals(purchaserEmail, that.purchaserEmail) &&
                Objects.equals(discoutId, that.discoutId) &&
                Objects.equals(datePurchased, that.datePurchased);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, purchaserEmail, pointsSpent, discoutId, datePurchased);
    }

    public Map<String, Object> generateMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("purchaserEmail", purchaserEmail);
        map.put("pointsSpent", pointsSpent);
        map.put("discountId", discoutId);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (purchaserEmail != null || includeEmptyFields)
            map.put("purchaserEmail", purchaserEmail);
        map.put("pointsSpent", pointsSpent);
        if (discoutId != null || includeEmptyFields)
            map.put("discountId", discoutId);

        return map;
    }
}
