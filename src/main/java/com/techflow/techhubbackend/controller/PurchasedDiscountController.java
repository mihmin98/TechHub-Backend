package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PurchasedDiscountModel;
import com.techflow.techhubbackend.service.PurchasedDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;

@RestController
@RequestMapping("/purchasedDiscount")
public class PurchasedDiscountController {

    @Autowired
    PurchasedDiscountService purchasedDiscountService;

    @GetMapping("")
    public List<PurchasedDiscountModel> getAllPurchasedDiscounts() throws ExecutionException, InterruptedException {
        return purchasedDiscountService.getAllPurchasedDiscounts();
    }

    @GetMapping("{id}")
    public PurchasedDiscountModel getPurchasedDiscount(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return purchasedDiscountService.getPurchasedDiscount(id);
    }

    @PostMapping("")
    public String createPurchasedDiscount(@RequestBody String discountId, @RequestHeader(AUTH_HEADER_STRING) String jwt) throws ExecutionException, InterruptedException, JsonProcessingException {
        return purchasedDiscountService.createPurchasedDiscount(discountId, jwt);
    }

    @PutMapping("{id}")
    public void updatePurchasedDiscount(@PathVariable("id") String id, @RequestBody PurchasedDiscountModel purchasedDiscountModel) throws ExecutionException, InterruptedException {
        purchasedDiscountService.updatePurchasedDiscount(id, purchasedDiscountModel);
    }

    @DeleteMapping("{id}")
    public void deletePurchasedDiscount(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        purchasedDiscountService.deletePurchasedDiscount(id);
    }

    @GetMapping("purchasedDiscountsByPurchaser/{purchaserEmail}")
    public List<PurchasedDiscountModel> getPurchasedDiscountsByPurchaser(@PathVariable("purchaserEmail") String purchaserEmail) throws ExecutionException, InterruptedException {
        return purchasedDiscountService.getPurchasedDiscountsByPurchaser(purchaserEmail);
    }
}
