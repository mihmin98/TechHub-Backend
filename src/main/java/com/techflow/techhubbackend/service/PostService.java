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
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
import com.techflow.techhubbackend.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@Service
public class PostService {

    public static final String COLLECTION_NAME = "post";
    public static final String THREAD_COLLECTION_NAME = "thread";
    public static final String USER_COLLECTION_NAME = "user";

    @Autowired
    ThreadService threadService;

    @Autowired
    UserService userService;

    @Autowired
    private Firestore dbFirestore;

    public List<PostModel> getAllPosts() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public PostModel getPost(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        PostModel postModel;
        if (documentReference.exists()) {
            postModel = new PostModel(Objects.requireNonNull(documentReference.getData()));
            postModel.setDateCreated(Objects.requireNonNull(documentReference.getCreateTime()).toDate());
            return postModel;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
        }
    }

    public String createPost(PostModel postModel) throws ExecutionException, InterruptedException, JsonProcessingException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        postModel.setId(documentReference.getId());
        postModel.setPostNumber(getPostsCountByThreadId(postModel.getThreadId()) + 1);
        documentReference.set(postModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("postId", documentReference.getId());

        return mapper.writeValueAsString(node);
    }

    public void updatePost(String id, PostModel postModel) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
    }

    public void deletePost(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        PostModel postModel = new PostModel(Objects.requireNonNull(documentReference.getData()));
        Long postNumber = postModel.getPostNumber();

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereGreaterThan("postNumber", postNumber).get().get().getDocuments();
        List<PostModel> postsToUpdate = documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
        for (PostModel postToUpdate : postsToUpdate) {
            postToUpdate.setPostNumber(postToUpdate.getPostNumber() - 1);
            updatePost(postToUpdate.getId(), postToUpdate);
        }

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<PostModel> getPostsByThreadId(String threadId) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(THREAD_COLLECTION_NAME).document(threadId).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("threadId", threadId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .sorted(Comparator.comparing(PostModel::getDateCreated))
                .collect(Collectors.toList());
    }

    public Long getPostsCountByThreadId(String threadId) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(THREAD_COLLECTION_NAME).document(threadId).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("threadId", threadId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .count();
    }

    public void upvotePost(String id, String jwt) throws ExecutionException, InterruptedException {
        PostModel postModel = getPost(id);
        Set<String> upvotes = postModel.getUpvotes();
        String userEmail = getEmailFromJWT(jwt);

        if (!upvotes.contains(userEmail)) {
            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());

            if (postModel.getDownvotes().contains(userEmail)) {
                postModel.getDownvotes().remove(userEmail);
                initialUserModel.setCurrentPoints(initialUserModel.getCurrentPoints() + 1);
                initialUserModel.setTotalPoints(initialUserModel.getTotalPoints() + 1);
            }

            upvotes.add(userEmail);
            postModel.setUpvotes(upvotes);

            userModel.setCurrentPoints(initialUserModel.getCurrentPoints() + 1);
            userModel.setTotalPoints(initialUserModel.getTotalPoints() + 1);
            userService.updateUserDetails(postModel.getUserEmail(), userModel);

            dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
        }
    }

    public void downvotePost(String id, String jwt) throws ExecutionException, InterruptedException {
        PostModel postModel = getPost(id);
        Set<String> downvotes = postModel.getDownvotes();
        String userEmail = getEmailFromJWT(jwt);

        if (!downvotes.contains(userEmail)) {
            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());

            if (postModel.getUpvotes().contains(userEmail)) {
                postModel.getUpvotes().remove(userEmail);
                initialUserModel.setCurrentPoints(initialUserModel.getCurrentPoints() - 1);
                initialUserModel.setTotalPoints(initialUserModel.getTotalPoints() - 1);
            }

            downvotes.add(userEmail);
            postModel.setDownvotes(downvotes);

            userModel.setCurrentPoints(initialUserModel.getCurrentPoints() - 1);
            userModel.setTotalPoints(initialUserModel.getTotalPoints() - 1);
            userService.updateUserDetails(postModel.getUserEmail(), userModel);

            dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
        }
    }

    public void removeUpvotePost(String id, String jwt) throws ExecutionException, InterruptedException {
        PostModel postModel = getPost(id);
        Set<String> upvotes = postModel.getUpvotes();
        String userEmail = getEmailFromJWT(jwt);

        if (upvotes.contains(userEmail)) {
            upvotes.remove(userEmail);
            postModel.setUpvotes(upvotes);

            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());
            userModel.setCurrentPoints(initialUserModel.getCurrentPoints() - 1);
            userModel.setTotalPoints(initialUserModel.getTotalPoints() - 1);
            userService.updateUserDetails(postModel.getUserEmail(), userModel);

            dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
        }
    }

    public void removeDownvotePost(String id, String jwt) throws ExecutionException, InterruptedException {
        PostModel postModel = getPost(id);
        Set<String> downvotes = postModel.getDownvotes();
        String userEmail = getEmailFromJWT(jwt);

        if (downvotes.contains(userEmail)) {
            downvotes.remove(userEmail);
            postModel.setDownvotes(downvotes);

            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());
            userModel.setCurrentPoints(initialUserModel.getCurrentPoints() + 1);
            userModel.setTotalPoints(initialUserModel.getTotalPoints() + 1);
            userService.updateUserDetails(postModel.getUserEmail(), userModel);

            dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
        }
    }

    public void awardTrophy(String id, String jwt) throws ExecutionException, InterruptedException {
        boolean vipStatus = getUserVipStatus(jwt);

        PostModel postModel = getPost(id);
        ThreadModel threadModel = new ThreadModel(threadService.getThread(postModel.getThreadId(), vipStatus));

        if (!postModel.isHasTrophy() && !threadModel.getHasTrophy()) {
            postModel.setHasTrophy(true);
            updatePost(id, postModel);

            threadModel.setHasTrophy(true);
            threadService.updateThread(postModel.getThreadId(), threadModel, threadModel.getVipStatus());

            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());
            userModel.setTrophies(initialUserModel.getTrophies() + 1);
            userService.updateUserDetails(postModel.getUserEmail(), userModel);
        }
    }

    private String getEmailFromJWT(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return decodedJWT.getSubject();
    }

    private boolean getUserVipStatus(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return decodedJWT.getClaim("userVipStatus").asBoolean();
    }
}
