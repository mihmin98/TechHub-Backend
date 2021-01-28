package com.techflow.techhubbackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.UserType;
import com.techflow.techhubbackend.model.PurchasedDiscountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@Service
public class DiscountService {

    public static final String COLLECTION_NAME = "discount";

    @Autowired
    private Firestore dbFirestore;

    public List<DiscountModel> getAllDiscounts() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> new DiscountModel(queryDocumentSnapshot.getData()))
                .collect(Collectors.toList());
    }

    public List<DiscountModel> getAllActiveDiscounts() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("isActive", true).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> new DiscountModel(queryDocumentSnapshot.getData()))
                .collect(Collectors.toList());
    }

    public DiscountModel getDiscount(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        DiscountModel discountModel;
        if (documentReference.exists()) {
            discountModel = new DiscountModel(Objects.requireNonNull(documentReference.getData()));
            return discountModel;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found");
        }
    }

    public String createDiscount(DiscountModel discountModel, String jwt) throws ExecutionException, InterruptedException, JsonProcessingException {

        if (getUserTypeFromJwt(jwt) != UserType.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only merchants can access this endpoint.");
        }

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        discountModel.setId(documentReference.getId());
        discountModel.setIsActive(true);
        documentReference.set(discountModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("discountId", documentReference.getId());

        return mapper.writeValueAsString(node);
    }

    public void updateDiscount(String id, DiscountModel discountModel, String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only merchants can access this endpoint.");
        }

        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(discountModel.generateMap(false)).get();
    }

    public void deleteDiscount(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public void markDiscountAsInactive(String id, String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MERCHANT) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only merchants can access this endpoint.");
        }

        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Discount not found");

        DiscountModel discountModel = new DiscountModel();
        discountModel.setIsActive(false);

        dbFirestore.collection(COLLECTION_NAME).document(id).update(discountModel.generateMap(false)).get();
    }

    private UserType getUserTypeFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return UserType.valueOf(decodedJWT.getClaim("userType").asString());
    }
  
    public List<DiscountModel> getDiscountsBySearch(String title, String jwt)
    throws InterruptedException, ExecutionException {
        // Get all discounts
        title = title.toLowerCase();
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();
        List<DiscountModel> allDiscounts = documentSnapshots.stream()
        .map(queryDocumentSnapshot -> new DiscountModel(queryDocumentSnapshot.getData()))
        .collect(Collectors.toList());

        // Make a list with the already purchased discounts of the user and active discounts

        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        String purchaserEmail = decodedJWT.getSubject();

        List<QueryDocumentSnapshot> purchasedDiscountsDocuments = dbFirestore.collection("purchasedDiscount").whereEqualTo("purchaserEmail", purchaserEmail).get().get().getDocuments();
        List<PurchasedDiscountModel> purchasedDiscounts = purchasedDiscountsDocuments.stream()
        .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
        .map(mapTimestampEntry -> new PurchasedDiscountModel(mapTimestampEntry.getKey()).builderSetDatePurchased(mapTimestampEntry.getValue()))
        .collect(Collectors.toList());
        List<String> streamedIds = purchasedDiscounts.stream().map(id -> id.getDiscountId()).collect(Collectors.toList());
        List<DiscountModel> returnList = new ArrayList<>();

        // Search for discounts

        for (var discount : allDiscounts) {
            if (discount.getTitle() == null)
                continue;
            if (discount.getIsActive() != true)
                continue;
            if(streamedIds.contains(discount.getId()))
                continue;
            Pattern pattern = Pattern.compile(title);
            Matcher matcherTitle = pattern.matcher(discount.getTitle().toLowerCase());
            if (matcherTitle.find()) {
                returnList.add(discount);
            }
        }

        return returnList;

        }
    }
