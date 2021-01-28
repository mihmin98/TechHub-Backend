package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.techflow.techhubbackend.model.DiscountModel;
import com.techflow.techhubbackend.service.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_HEADER_STRING;

@RestController
@RequestMapping("/discount")
public class DiscountController {

    @Autowired
    DiscountService discountService;

    @GetMapping("")
    public List<DiscountModel> getAllDiscounts() throws ExecutionException, InterruptedException {
        return discountService.getAllActiveDiscounts();
    }

    @GetMapping("{id}")
    public DiscountModel getDiscount(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        return discountService.getDiscount(id);
    }

    @PostMapping("")
    public String createDiscount(@RequestBody DiscountModel discountModel) throws ExecutionException, InterruptedException, JsonProcessingException {
        return discountService.createDiscount(discountModel);
    }

    @PutMapping("{id}")
    public void updateDiscount(@PathVariable("id") String id, @RequestBody DiscountModel discountModel) throws ExecutionException, InterruptedException {
        discountService.updateDiscount(id, discountModel);
    }

    @DeleteMapping("{id}")
    public void deleteDiscount(@PathVariable("id") String id) throws ExecutionException, InterruptedException {
        discountService.markDiscountAsInactive(id);
    }

    @GetMapping("/search/{title}")
    public List<DiscountModel> searchDiscounts(@PathVariable("title") String title, @RequestHeader(AUTH_HEADER_STRING) String jwt)
            throws InterruptedException, ExecutionException {
        return discountService.getDiscountsBySearch(title, jwt);
    }
}
