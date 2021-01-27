package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.DiscountControllerTestDataProperties;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
import com.techflow.techhubbackend.model.DiscountModel;
import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.model.UserType;
import com.techflow.techhubbackend.security.SecurityConstants;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private DiscountControllerTestDataProperties discountTestDataProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private String jwt = null;

    private DiscountModel testDiscountModel;

    private final List<String> discountsToDelete = new ArrayList<>();

    private final String USER_COLLECTION_NAME = "user";
    private final String DISCOUNTS_COLLECTION_NAME = "discount";


    @BeforeAll
    void login() throws Exception {
        testDiscountModel = new DiscountModel(null,
                discountTestDataProperties.getDiscountSellerEmail(),
                discountTestDataProperties.getDiscountTitle(),
                discountTestDataProperties.getDiscountDescription(),
                discountTestDataProperties.getDiscountPictures(),
                discountTestDataProperties.getDiscountPointsCost(),
                discountTestDataProperties.getDiscountVipStatus(),
                discountTestDataProperties.getDiscountIsActive());

        UserModel user = new UserModel(userTestDataProperties.getUserEmail(),
                userTestDataProperties.getUserPassword(),
                userTestDataProperties.getUserUsername(),
                UserType.REGULAR_USER,
                userTestDataProperties.getUserProfilePicture(),
                userTestDataProperties.getUserAccountStatus(),
                userTestDataProperties.getUserTotalPoints(),
                userTestDataProperties.getUserCurrentPoints(),
                userTestDataProperties.getUserTrophies(),
                userTestDataProperties.getUserVipVipStatus(),
                userTestDataProperties.getUserRafflesWon());

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        dbFirestore.collection(USER_COLLECTION_NAME).document(user.getEmail()).set(user.generateMap()).get();
        user.setPassword(userTestDataProperties.getUserPassword());

        // Login
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("email", user.getEmail());
        node.put("password", user.getPassword());
        String testUserLoginJson = mapper.writeValueAsString(node);

        String loginResult = mockMvc.perform(post("/login")
                .content(testUserLoginJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        HashMap<String, String> obj = mapper.readValue(loginResult, HashMap.class);
        jwt = obj.get("accessToken");
    }

    @Test
    void getAllDiscounts() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a discount
        DiscountModel discount = new DiscountModel(testDiscountModel);
        DocumentReference documentReference = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document();
        discount.setId(documentReference.getId());

        documentReference.set(discount.generateMap()).get();

        discountsToDelete.add(discount.getId());

        // Get all discounts
        String discountsJson = mockMvc.perform(get("/discount")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<DiscountModel> receivedDiscounts = mapper.readValue(discountsJson, new TypeReference<>() {
        });

        // Check if the created discount is in the list
        boolean foundDiscount = false;

        for (DiscountModel d : receivedDiscounts) {
            if (d.getId().equals(discount.getId())) {
                foundDiscount = true;
                break;
            }
        }

        assertTrue(receivedDiscounts.size() > 0);
        assertTrue(foundDiscount);
    }

    @Test
    void getDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Get non existing discount
        DocumentReference documentReference = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(get("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create discount
        DiscountModel discount = new DiscountModel(testDiscountModel);
        discount.setId(documentReference.getId());

        documentReference.set(discount.generateMap()).get();

        discountsToDelete.add(discount.getId());

        // Get discount
        String discountJson = mockMvc.perform(get("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        DiscountModel receivedDiscount = mapper.readValue(discountJson, DiscountModel.class);

        // Check the received discount
        assertEquals(discount.getId(), receivedDiscount.getId());
        assertEquals(discount.getSellerEmail(), receivedDiscount.getSellerEmail());
        assertEquals(discount.getTitle(), receivedDiscount.getTitle());
        assertEquals(discount.getDescription(), receivedDiscount.getDescription());
        assertEquals(discount.getPictures(), receivedDiscount.getPictures());
        assertEquals(discount.getPointsCost(), receivedDiscount.getPointsCost());
        assertEquals(discount.getVipStatus(), receivedDiscount.getVipStatus());
        assertEquals(discount.getIsActive(), receivedDiscount.getIsActive());
    }

    @Test
    void postDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create discount
        DiscountModel discount = new DiscountModel(testDiscountModel);

        String discountIdJson = mockMvc.perform(post("/discount")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(discount))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String discountId = (String) mapper.readValue(discountIdJson, HashMap.class).get("discountId");
        discountsToDelete.add(discountId);

        // Get created discount from db
        DocumentSnapshot documentSnapshot = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document(discountId).get().get();
        DiscountModel createdDiscount = new DiscountModel(Objects.requireNonNull(documentSnapshot.getData()));

        // Check the received discount
        assertEquals(discountId, createdDiscount.getId());
        assertEquals(discount.getSellerEmail(), createdDiscount.getSellerEmail());
        assertEquals(discount.getTitle(), createdDiscount.getTitle());
        assertEquals(discount.getDescription(), createdDiscount.getDescription());
        assertEquals(discount.getPictures(), createdDiscount.getPictures());
        assertEquals(discount.getPointsCost(), createdDiscount.getPointsCost());
        assertEquals(discount.getVipStatus(), createdDiscount.getVipStatus());
        assertEquals(discount.getIsActive(), createdDiscount.getIsActive());
    }

    @Test
    void putDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Put non existing discount
        DocumentReference documentReference = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(put("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create discount
        DiscountModel discount = new DiscountModel(testDiscountModel);
        discount.setId(documentReference.getId());

        documentReference.set(discount.generateMap()).get();

        discountsToDelete.add(discount.getId());

        // Update a field and put
        DiscountModel putDiscount = new DiscountModel();
        putDiscount.setDescription(discountTestDataProperties.getDiscountPutDescription());

        mockMvc.perform(put("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putDiscount))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get updated discount from db
        DocumentSnapshot documentSnapshot = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document(discount.getId()).get().get();
        DiscountModel updatedDiscount = new DiscountModel(Objects.requireNonNull(documentSnapshot.getData()));

        // Check the received discount
        assertEquals(discount.getId(), updatedDiscount.getId());
        assertEquals(discount.getSellerEmail(), updatedDiscount.getSellerEmail());
        assertEquals(discount.getTitle(), updatedDiscount.getTitle());
        assertEquals(discountTestDataProperties.getDiscountPutDescription(), updatedDiscount.getDescription());
        assertEquals(discount.getPictures(), updatedDiscount.getPictures());
        assertEquals(discount.getPointsCost(), updatedDiscount.getPointsCost());
        assertEquals(discount.getVipStatus(), updatedDiscount.getVipStatus());
        assertEquals(discount.getIsActive(), updatedDiscount.getIsActive());
    }

    @Test
    void deleteDiscount() throws Exception {
        // Delete non existing discount
        DocumentReference documentReference = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(delete("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a discount
        DiscountModel discount = new DiscountModel(testDiscountModel);
        discount.setId(documentReference.getId());

        dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document(discount.getId()).set(discount.generateMap()).get();

        discountsToDelete.add(discount.getId());

        // Delete discount
        mockMvc.perform(delete("/discount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check that the discount has been marked as inactive
        assertFalse(Objects.requireNonNull(documentReference.get().get().getBoolean("isActive")));
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).delete().get();

        for (String id : discountsToDelete)
            dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document(id).delete();
    }
}
