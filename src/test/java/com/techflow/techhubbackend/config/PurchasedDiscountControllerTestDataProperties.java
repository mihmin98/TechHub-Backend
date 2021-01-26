package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:PurchasedDiscountControllerTest.properties")
public class PurchasedDiscountControllerTestDataProperties {

    @Value("${purchasedDiscount.purchaserEmail}")
    private String purchasedDiscountPurchaserEmail;

    @Value("${purchasedDiscount.pointsSpent}")
    private Integer purchasedDiscountPointsSpent;

    @Value("${purchasedDiscount.put.purchaserEmail}")
    private String purchasedDiscountPutPurchaserEmail;

    public PurchasedDiscountControllerTestDataProperties() {
    }

    public String getPurchasedDiscountPurchaserEmail() {
        return purchasedDiscountPurchaserEmail;
    }

    public Integer getPurchasedDiscountPointsSpent() {
        return purchasedDiscountPointsSpent;
    }

    public String getPurchasedDiscountPutPurchaserEmail() {
        return purchasedDiscountPutPurchaserEmail;
    }
}
