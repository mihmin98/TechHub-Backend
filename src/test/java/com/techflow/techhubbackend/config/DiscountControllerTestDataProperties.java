package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:DiscountControllerTest.properties")
public class DiscountControllerTestDataProperties {

    @Value("${discount.sellerEmail}")
    private String discountSellerEmail;

    @Value("${discount.title}")
    private String discountTitle;

    @Value("${discount.description}")
    private String discountDescription;

    @Value("#{${discount.pictures}}")
    private List<String> discountPictures;

    @Value("${discount.pointsCost}")
    private Integer discountPointsCost;

    @Value("${discount.vipStatus}")
    private Boolean discountVipStatus;

    @Value("${discount.isActive}")
    private Boolean discountIsActive;

    @Value("${discount.put.description}")
    private String discountPutDescription;

    public DiscountControllerTestDataProperties() {
    }

    public String getDiscountSellerEmail() {
        return discountSellerEmail;
    }

    public String getDiscountTitle() {
        return discountTitle;
    }

    public String getDiscountDescription() {
        return discountDescription;
    }

    public List<String> getDiscountPictures() {
        return discountPictures;
    }

    public Integer getDiscountPointsCost() {
        return discountPointsCost;
    }

    public Boolean getDiscountVipStatus() {
        return discountVipStatus;
    }

    public Boolean getDiscountIsActive() {
        return discountIsActive;
    }

    public String getDiscountPutDescription() {
        return discountPutDescription;
    }
}
