package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.PurchasedDiscountModel;
import com.techflow.techhubbackend.service.PurchasedDiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

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
    public String createPurchasedDiscount(@RequestBody PurchasedDiscountModel purchasedDiscountModel) throws ExecutionException, InterruptedException, JsonProcessingException {
        return purchasedDiscountService.createPurchasedDiscount(purchasedDiscountModel);
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
