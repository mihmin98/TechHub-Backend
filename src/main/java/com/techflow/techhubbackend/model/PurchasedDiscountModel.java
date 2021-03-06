package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PurchasedDiscountModel {

    private String id;
    private String purchaserEmail;
    private Integer pointsSpent;
    private String discountId;
    private Date datePurchased;
    private String code;

    public PurchasedDiscountModel() {
    }
    public PurchasedDiscountModel(String id, String purchaserEmail, Integer pointsSpent, String discountId, Date datePurchased, String code) {
        this.id = id;
        this.purchaserEmail = purchaserEmail;
        this.pointsSpent = pointsSpent;
        this.discountId = discountId;
        this.datePurchased = datePurchased;
        this.code = code;
    }

    public PurchasedDiscountModel(PurchasedDiscountModel purchasedDiscountModel) {
        this.id = purchasedDiscountModel.id;
        this.purchaserEmail = purchasedDiscountModel.purchaserEmail;
        this.pointsSpent = purchasedDiscountModel.pointsSpent;
        this.discountId = purchasedDiscountModel.discountId;
        this.datePurchased = purchasedDiscountModel.datePurchased;
        this.code = purchasedDiscountModel.code;
    }

    public PurchasedDiscountModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.purchaserEmail = (String) map.getOrDefault("purchaserEmail", "no purchaser email");
        this.pointsSpent = ((Long) map.getOrDefault("pointsSpent", 0)).intValue();
        this.discountId = (String) map.getOrDefault("discountId", "no discount id");
        this.code = (String) map.getOrDefault("code", "no code");
    }

    public String getId() {
        return id;
    }

    public String getPurchaserEmail() {
        return purchaserEmail;
    }

    public Integer getPointsSpent() {
        return pointsSpent;
    }

    public String getDiscountId() {
        return discountId;
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

    public void setPointsSpent(Integer pointsSpent) {
        this.pointsSpent = pointsSpent;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public void setDatePurchased(Date datePurchased) {
        this.datePurchased = datePurchased;
    }

    public PurchasedDiscountModel builderSetDatePurchased(Timestamp datePurchased) {
        this.datePurchased = datePurchased.toDate();
        return this;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "PurchasedDiscountModel{" +
                "id='" + id + '\'' +
                ", purchaserEmail='" + purchaserEmail + '\'' +
                ", pointsSpent=" + pointsSpent +
                ", discountId='" + discountId + '\'' +
                ", datePurchased=" + datePurchased +
                ", code=" + code +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PurchasedDiscountModel that = (PurchasedDiscountModel) o;
        return Objects.equals(pointsSpent, that.pointsSpent) &&
                Objects.equals(id, that.id) &&
                Objects.equals(purchaserEmail, that.purchaserEmail) &&
                Objects.equals(discountId, that.discountId) &&
                Objects.equals(datePurchased, that.datePurchased) &&
                Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, purchaserEmail, pointsSpent, discountId, datePurchased, code);
    }

    public Map<String, Object> generateMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("purchaserEmail", purchaserEmail);
        map.put("pointsSpent", pointsSpent);
        map.put("discountId", discountId);
        map.put("code", code);
        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (purchaserEmail != null || includeEmptyFields)
            map.put("purchaserEmail", purchaserEmail);
        if (pointsSpent != null || includeEmptyFields)
            map.put("pointsSpent", pointsSpent);
        if (discountId != null || includeEmptyFields)
            map.put("discountId", discountId);
        if (code != null || includeEmptyFields)
            map.put("code", code);
        return map;
    }
}
