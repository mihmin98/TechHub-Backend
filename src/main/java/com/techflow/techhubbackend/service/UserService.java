package com.techflow.techhubbackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.model.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

//CRUD operations
@Service
public class UserService implements UserDetailsService {

    public static final String COL_NAME = "user";

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public void saveUserDetails(UserModel user, boolean encodePassword) throws InterruptedException, ExecutionException {
        // Check if user already exists
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(user.getEmail());
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if (future.get().exists()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already exists");
        }

        if (encodePassword)
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        dbFirestore.collection(COL_NAME).document(user.getEmail()).set(user.generateMap()).get();
    }

    public UserModel getUserDetails(String email) throws InterruptedException, ExecutionException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        DocumentSnapshot document = future.get();

        if (document.exists()) {
            UserModel user = new UserModel(Objects.requireNonNull(document.getData()));
            user.setPassword("");
            return user;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
    }

    public void updateUserDetails(String email, UserModel user) throws InterruptedException, ExecutionException {
        // Check if user exists
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if (!future.get().exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        // Check if password has changed
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        }

        // Check if email has changed, if yes create a new document with previous contents, delete old document, and then update new document; maybe can optimize to not have to update
        if (user.getEmail() != null && !user.getEmail().equals(email)) {
            UserModel oldUser = new UserModel(Objects.requireNonNull(future.get().getData()));
            oldUser.setEmail(user.getEmail());
            saveUserDetails(oldUser, false);
            deleteUser(email);

            email = user.getEmail();
        }

        dbFirestore.collection(COL_NAME).document(email).update(user.generateMap(false)).get();
    }

    public void deleteUser(String email) throws ExecutionException, InterruptedException {
        // Check if user exists
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        if (!future.get().exists()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        dbFirestore.collection(COL_NAME).document(email).delete().get();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        DocumentReference documentReference = dbFirestore.collection(COL_NAME).document(username);
        ApiFuture<DocumentSnapshot> future = documentReference.get();

        try {
            DocumentSnapshot document = future.get();
            UserModel user;

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

    public List<UserModel> SortUsersByPointsAndTrophies(Integer userCount) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COL_NAME).whereEqualTo("type", UserType.REGULAR_USER.toString()).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> new UserModel(queryDocumentSnapshot.getData()))
                .sorted(Comparator.reverseOrder())
                .limit(userCount)
                .collect(Collectors.toList());
    }

    public UserModel getCurrentUser(String jwt) throws ExecutionException, InterruptedException {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return getUserDetails(decodedJWT.getSubject());
    }
}
