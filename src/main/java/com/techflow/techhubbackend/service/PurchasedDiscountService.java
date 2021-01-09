package com.techflow.techhubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.PurchasedDiscountModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PurchasedDiscountService {

    public static final String COLLECTION_NAME = "purchasedDiscount";

    @Autowired
    private Firestore dbFirestore;

    public List<PurchasedDiscountModel> getAllPurchasedDiscounts() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PurchasedDiscountModel(mapTimestampEntry.getKey()).builderSetDatePurchased(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public PurchasedDiscountModel getPurchasedDiscount(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        PurchasedDiscountModel purchasedDiscountModel;
        if (documentReference.exists()) {
            purchasedDiscountModel = new PurchasedDiscountModel(Objects.requireNonNull(documentReference.getData()));
            purchasedDiscountModel.setDatePurchased(Objects.requireNonNull(documentReference.getCreateTime()).toDate());
            return purchasedDiscountModel;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchased discount not found");
        }
    }

    public String createPurchasedDiscount(PurchasedDiscountModel purchasedDiscountModel) throws ExecutionException, InterruptedException, JsonProcessingException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        purchasedDiscountModel.setId(documentReference.getId());
        documentReference.set(purchasedDiscountModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("purchasedDiscountModelId", documentReference.getId());

        return mapper.writeValueAsString(node);

    }

    public void updatePurchasedDiscount(String id, PurchasedDiscountModel purchasedDiscountModel) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchased discount not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(purchasedDiscountModel.generateMap(false)).get();
    }

    public void deletePurchasedDiscount(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchased discount not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }
}
