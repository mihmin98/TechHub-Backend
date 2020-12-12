package com.techflow.techhubbackend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.techflow.techhubbackend.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.ExecutionException;

//CRUD operations
@Service
public class UserService implements UserDetailsService {
    public static final String COL_NAME = "user";

    @Autowired
    private Firestore dbFirestore;

    public String saveUserDetails(UserModel user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_NAME).document(user.getEmail()).set(user.getMap());
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public UserModel getUserDetails(String username) throws InterruptedException, ExecutionException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(username);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = future.get();

        UserModel user = null;

        if (document.exists()) {
            user = document.toObject(UserModel.class);
            return user;
        } else {
            return null;
        }
    }

    public String updateUserDetails(UserModel user) throws InterruptedException, ExecutionException {
        ApiFuture<WriteResult> collectionsApiFuture = dbFirestore.collection(COL_NAME).document(user.getUsername()).set(user.getMap());
        return collectionsApiFuture.get().getUpdateTime().toString();
    }

    public String deleteUser(String username) {
        ApiFuture<WriteResult> writeResult = dbFirestore.collection(COL_NAME).document(username).delete();
        return "Document with username " + username + " has been deleted";
    }

    public UserModel requestUserLogIn(String email, String password) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = future.get();

        UserModel user = null;

        if (document.exists()) {
            user = document.toObject(UserModel.class);
            if (user != null && password.equals(user.getPassword())) {
                return user;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(username);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();
            UserModel user = null;

            if (document.exists()) {
                user = document.toObject(UserModel.class);
                if (user != null) {
                    return new User(user.getEmail(), user.getPassword(), Collections.emptyList());
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        throw new UsernameNotFoundException(username);
    }
}
