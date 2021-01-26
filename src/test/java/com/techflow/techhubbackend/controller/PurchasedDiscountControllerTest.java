package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.DiscountControllerTestDataProperties;
import com.techflow.techhubbackend.config.PurchasedDiscountControllerTestDataProperties;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
import com.techflow.techhubbackend.model.DiscountModel;
import com.techflow.techhubbackend.model.PurchasedDiscountModel;
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
public class PurchasedDiscountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private DiscountControllerTestDataProperties discountTestDataProperties;

    @Autowired
    private PurchasedDiscountControllerTestDataProperties purchasedDiscountTestDataProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private String jwt = null;

    private PurchasedDiscountModel testPurchasedDiscountModel;
    private DiscountModel testDiscountModel;

    private final List<String> discountsToDelete = new ArrayList<>();
    private final List<String> purchasedDiscountsToDelete = new ArrayList<>();

    private final String USER_COLLECTION_NAME = "user";
    private final String DISCOUNTS_COLLECTION_NAME = "discount";
    private final String PURCHASED_DISCOUNTS_COLLECTION_NAME = "purchasedDiscount";


    @BeforeAll
    void login() throws Exception {
        testPurchasedDiscountModel = new PurchasedDiscountModel(null,
                purchasedDiscountTestDataProperties.getPurchasedDiscountPurchaserEmail(),
                purchasedDiscountTestDataProperties.getPurchasedDiscountPointsSpent(),
                null,
                null);

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
    void getAllPurchasedDiscounts() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a purchased discount
        PurchasedDiscountModel purchasedDiscount = new PurchasedDiscountModel(testPurchasedDiscountModel);
        DocumentReference documentReference = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document();
        purchasedDiscount.setId(documentReference.getId());

        documentReference.set(purchasedDiscount.generateMap()).get();

        purchasedDiscountsToDelete.add(purchasedDiscount.getId());

        // Get all purchased discounts
        String purchasedDiscountsJson = mockMvc.perform(get("/purchasedDiscount")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PurchasedDiscountModel> receivedPurchasedDiscounts = mapper.readValue(purchasedDiscountsJson, new TypeReference<>() {
        });

        // Check if the created purchased discount is in the list
        boolean foundPurchasedDiscount = false;

        for (PurchasedDiscountModel pd : receivedPurchasedDiscounts) {
            if (pd.getId().equals(purchasedDiscount.getId())) {
                foundPurchasedDiscount = true;
                break;
            }
        }

        assertTrue(receivedPurchasedDiscounts.size() > 0);
        assertTrue(foundPurchasedDiscount);
    }

    @Test
    void getPurchasedDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Get non existing purchased discount
        DocumentReference documentReference = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(get("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create purchased discount
        PurchasedDiscountModel purchasedDiscount = new PurchasedDiscountModel(testPurchasedDiscountModel);
        purchasedDiscount.setId(documentReference.getId());

        documentReference.set(purchasedDiscount.generateMap()).get();

        purchasedDiscountsToDelete.add(purchasedDiscount.getId());

        // Get purchased discount
        String purchasedDiscountJson = mockMvc.perform(get("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PurchasedDiscountModel receivedPurchasedDiscount = mapper.readValue(purchasedDiscountJson, PurchasedDiscountModel.class);

        // Check the received purchased discount
        assertEquals(purchasedDiscount.getId(), receivedPurchasedDiscount.getId());
        assertEquals(purchasedDiscount.getPurchaserEmail(), receivedPurchasedDiscount.getPurchaserEmail());
        assertEquals(purchasedDiscount.getPointsSpent(), receivedPurchasedDiscount.getPointsSpent());
        assertEquals(purchasedDiscount.getDiscountId(), receivedPurchasedDiscount.getDiscountId());
        assertNotNull(receivedPurchasedDiscount.getDatePurchased());
    }

    @Test
    void postPurchasedDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a discount
        DocumentReference discountDocumentReference = dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document();

        DiscountModel discount = new DiscountModel(testDiscountModel);
        discount.setId(discountDocumentReference.getId());

        discountDocumentReference.set(discount.generateMap()).get();

        discountsToDelete.add(discount.getId());

        // Try to purchase discount with less points than required
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).update("currentPoints", 0).get();

        ObjectNode node = mapper.createObjectNode();
        node.put("discountId", discount.getId());

        mockMvc.perform(post("/purchasedDiscount")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(discount.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotAcceptable());

        // Purchase discount
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).update("currentPoints", discount.getPointsCost()).get();

        String purchasedDiscountIdJson = mockMvc.perform(post("/purchasedDiscount")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(discount.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String purchasedDiscountId = (String) mapper.readValue(purchasedDiscountIdJson, HashMap.class).get("purchasedDiscountModelId");

        // Check if the user points have been updated
        long updatedUserPoints = Objects.requireNonNull(dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).get().get().getLong("currentPoints"));
        assertEquals(0, updatedUserPoints);

        // Get created discount from db
        DocumentSnapshot documentSnapshot = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document(purchasedDiscountId).get().get();
        PurchasedDiscountModel createdPurchasedDiscount = new PurchasedDiscountModel(Objects.requireNonNull(documentSnapshot.getData()));

        // Check the received discount
        assertEquals(purchasedDiscountId, createdPurchasedDiscount.getId());
        assertEquals(userTestDataProperties.getUserEmail(), createdPurchasedDiscount.getPurchaserEmail());
        assertEquals(discount.getPointsCost(), createdPurchasedDiscount.getPointsSpent());
        assertEquals(discount.getId(), createdPurchasedDiscount.getDiscountId());
    }

    @Test
    void putPurchasedDiscount() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Put non existing purchased discount
        DocumentReference documentReference = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(put("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create discount
        PurchasedDiscountModel purchasedDiscount = new PurchasedDiscountModel(testPurchasedDiscountModel);
        purchasedDiscount.setId(documentReference.getId());

        documentReference.set(purchasedDiscount.generateMap()).get();

        purchasedDiscountsToDelete.add(purchasedDiscount.getId());

        // Update a field and put
        PurchasedDiscountModel putPurchasedDiscount = new PurchasedDiscountModel();
        putPurchasedDiscount.setPurchaserEmail(purchasedDiscountTestDataProperties.getPurchasedDiscountPutPurchaserEmail());

        mockMvc.perform(put("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putPurchasedDiscount))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Get updated purchased discount from db
        DocumentSnapshot documentSnapshot = documentReference.get().get();
        PurchasedDiscountModel updatedPurchasedDiscount = new PurchasedDiscountModel(Objects.requireNonNull(documentSnapshot.getData()));

        // Check the received discount
        assertEquals(purchasedDiscount.getId(), updatedPurchasedDiscount.getId());
        assertEquals(purchasedDiscountTestDataProperties.getPurchasedDiscountPutPurchaserEmail(), updatedPurchasedDiscount.getPurchaserEmail());
        assertEquals(purchasedDiscount.getPointsSpent(), updatedPurchasedDiscount.getPointsSpent());
        assertEquals(purchasedDiscount.getDiscountId(), updatedPurchasedDiscount.getDiscountId());
    }

    @Test
    void deletePurchasedDiscount() throws Exception {
        // Delete non existing discount
        DocumentReference documentReference = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document();

        mockMvc.perform(delete("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a discount
        PurchasedDiscountModel purchasedDiscount = new PurchasedDiscountModel(testPurchasedDiscountModel);
        purchasedDiscount.setId(documentReference.getId());

        documentReference.set(purchasedDiscount.generateMap()).get();

        // Delete purchased discount
        mockMvc.perform(delete("/purchasedDiscount/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check that the purchased discount has been deleted
        assertFalse(documentReference.get().get().exists());
    }

    @Test
    void getPurchasedDiscountsByPurchaser() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a purchased discount
        PurchasedDiscountModel purchasedDiscount = new PurchasedDiscountModel(testPurchasedDiscountModel);
        DocumentReference documentReference = dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document();
        purchasedDiscount.setId(documentReference.getId());
        purchasedDiscount.setPurchaserEmail(userTestDataProperties.getUserEmail());

        documentReference.set(purchasedDiscount.generateMap()).get();

        purchasedDiscountsToDelete.add(purchasedDiscount.getId());

        // Get all purchased discounts that belong to the current user
        String purchasedDiscountsJson = mockMvc.perform(get("/purchasedDiscount/purchasedDiscountsByPurchaser/" + userTestDataProperties.getUserEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PurchasedDiscountModel> receivedPurchasedDiscounts = mapper.readValue(purchasedDiscountsJson, new TypeReference<>() {
        });

        // Check that the created purchased discount is in the list, and that all elements have the same purchaser
        boolean foundPurchasedDiscount = false;

        for (PurchasedDiscountModel pd : receivedPurchasedDiscounts) {
            assertEquals(userTestDataProperties.getUserEmail(), pd.getPurchaserEmail());
            if (pd.getId().equals(purchasedDiscount.getId())) {
                foundPurchasedDiscount = true;
                break;
            }
        }

        assertTrue(receivedPurchasedDiscounts.size() > 0);
        assertTrue(foundPurchasedDiscount);
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).delete().get();

        for (String id : discountsToDelete)
            dbFirestore.collection(DISCOUNTS_COLLECTION_NAME).document(id).delete().get();

        for (String id : purchasedDiscountsToDelete)
            dbFirestore.collection(PURCHASED_DISCOUNTS_COLLECTION_NAME).document(id).delete().get();
    }
}
