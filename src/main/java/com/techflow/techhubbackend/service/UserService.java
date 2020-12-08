package com.techflow.techhubbackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.techflow.techhubbackend.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

//CRUD operations
@Service
public class UserService {
    public static final String COL_NAME="user";

    @Autowired
    private Firestore dbFirestore;

    public String saveUserDetails(UserModel user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_NAME).document(user.getUsername()).set(user.getMap());
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public UserModel getUserDetails(String name) throws InterruptedException, ExecutionException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(name);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = future.get();

        UserModel user = null;

        if(document.exists()) {
            user = document.toObject(UserModel.class);
            return user;
        }else {
            return null;
        }
    }

    public String updateUserDetails(UserModel user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_NAME).document(user.getUsername()).set(user.getMap());
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteUser(String name) {
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COL_NAME).document(name).delete();
        return "Document with User ID "+name+" has been deleted";
    }

}
