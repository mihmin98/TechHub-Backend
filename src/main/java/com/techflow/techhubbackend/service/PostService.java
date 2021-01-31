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
import com.techflow.techhubbackend.model.*;
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
    ReportService reportService;

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
        postModel.setHasTrophy(false);
        documentReference.set(postModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("postId", documentReference.getId());

        return mapper.writeValueAsString(node);
    }

    public void updatePost(String id, PostModel postModel, UserType userType) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentSnapshot.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        if (Objects.requireNonNull(documentSnapshot.getBoolean("hasTrophy")) && userType == UserType.REGULAR_USER)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User cannot edit a post with an award");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();
    }

    public void deletePost(String id, UserType userType) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentSnapshot.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        if (Objects.requireNonNull(documentSnapshot.getBoolean("hasTrophy")) && userType == UserType.REGULAR_USER)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User cannot edit a post with an award");

        PostModel postModel = new PostModel(Objects.requireNonNull(documentSnapshot.getData()));

        //revoke user points if his/her post is deleted
        UserModel userModel = new UserModel();
        UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());
        userModel.setType(UserType.REGULAR_USER);
        userModel.setTotalPoints(initialUserModel.getTotalPoints() - postModel.getUpvotes().size());
        userModel.setCurrentPoints(initialUserModel.getCurrentPoints() - postModel.getUpvotes().size());
        if (postModel.isHasTrophy()) {

            //revoke trophy
            userModel.setTrophies(initialUserModel.getTrophies() - 1);

            //revoke hasTrophy status to thread
            ThreadModel threadModel = new ThreadModel();
            threadModel.setHasTrophy(false);
            threadService.updateThread(postModel.getThreadId(), threadModel, true);
        }
        userService.updateUserDetails(postModel.getUserEmail(), userModel);

        //update the other posts number
        Long postNumber = postModel.getPostNumber();

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereGreaterThan("postNumber", postNumber).get().get().getDocuments();
        List<PostModel> postsToUpdate = documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
        for (PostModel postToUpdate : postsToUpdate) {
            postToUpdate.setPostNumber(postToUpdate.getPostNumber() - 1);
            updatePost(postToUpdate.getId(), postToUpdate, UserType.NO_TYPE);
        }

        //delete all reports attached to the post
        List<ReportModel> reportsAttachedToPost = reportService.getReportsByReportedItemIdIdUnauthorized(postModel.getId());
        for (ReportModel report : reportsAttachedToPost) {
            reportService.deleteReport(report.getId());
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

            if (initialUserModel.getType() == UserType.MODERATOR)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Moderators can't be upvoted");

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

            if (!userModel.isVipStatus() && ((userModel.getTotalPoints() + userModel.getTrophies() * 10) >= 1000))
                userModel.setVipStatus(true);

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

            if (initialUserModel.getType() == UserType.MODERATOR)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Moderators can't be downvoted");

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
            UserModel initialUserModel = userService.getUserDetails(postModel.getUserEmail());

            if (initialUserModel.getType() == UserType.MODERATOR)
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Moderators can't be awarded trophies");

            postModel.setHasTrophy(true);
            updatePost(id, postModel, UserType.NO_TYPE);

            threadModel.setHasTrophy(true);
            threadService.updateThread(postModel.getThreadId(), threadModel, threadModel.getVipStatus());

            UserModel userModel = new UserModel();
            userModel.setType(initialUserModel.getType());
            userModel.setTrophies(initialUserModel.getTrophies() + 1);

            if (!userModel.isVipStatus() && ((userModel.getTotalPoints() + userModel.getTrophies() * 10) >= 1000))
                userModel.setVipStatus(true);

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
