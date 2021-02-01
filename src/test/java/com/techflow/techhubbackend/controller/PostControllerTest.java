package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.PostControllerTestDataProperties;
import com.techflow.techhubbackend.config.ThreadControllerTestDataProperties;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
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

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private ThreadControllerTestDataProperties threadControllerTestDataProperties;

    @Autowired
    private PostControllerTestDataProperties postControllerTestDataProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private String jwt = null;

    private PostModel testPostModel;

    private final List<String> threadsToDelete = new ArrayList<>();
    private final List<String> postsToDelete = new ArrayList<>();

    private final String USER_COLLECTION_NAME = "user";
    private final String THREADS_COLLECTION_NAME = "thread";
    private final String POSTS_COLLECTION_NAME = "post";

    @BeforeAll
    void login() throws Exception {
        testPostModel = new PostModel(null,
                postControllerTestDataProperties.getPostUserEmail(),
                null,
                null,
                postControllerTestDataProperties.getPostText(),
                null,
                postControllerTestDataProperties.getPostHasTrophy(),
                postControllerTestDataProperties.getUpvotes(),
                postControllerTestDataProperties.getDownvotes(),
                postControllerTestDataProperties.getIsReported());

        UserModel user = new UserModel(userTestDataProperties.getUserEmail(),
                userTestDataProperties.getUserPassword(),
                userTestDataProperties.getUserUsername(),
                UserType.REGULAR_USER,
                userTestDataProperties.getUserProfilePicture(),
                userTestDataProperties.getUserAccountStatus(),
                userTestDataProperties.getUserTotalPoints(),
                userTestDataProperties.getUserCurrentPoints(),
                userTestDataProperties.getUserTrophies(),
                false,
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
    void getAllPosts() throws Exception {
        // Create a post
        PostModel post = new PostModel(testPostModel);
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(documentReference.getId());

        // Get all posts
        String postsJson = mockMvc.perform(get("/post")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<PostModel> receivedPosts = mapper.readValue(postsJson, new TypeReference<>() {
        });

        boolean foundPost = false;

        for (PostModel p : receivedPosts) {
            if (p.getId().equals(documentReference.getId())) {
                foundPost = true;
                break;
            }
        }

        assertTrue(receivedPosts.size() > 0);
        assertTrue(foundPost);
    }

    @Test
    void getPost() throws Exception {
        PostModel post = new PostModel(testPostModel);
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();

        // Try to get non existing post
        mockMvc.perform(get("/post/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a post
        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(documentReference.getId());

        // Get post
        String postJson = mockMvc.perform(get("/post/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        PostModel receivedPost = mapper.readValue(postJson, PostModel.class);

        assertEquals(post.getId(), receivedPost.getId());
        assertEquals(post.getUserEmail(), receivedPost.getUserEmail());
        assertEquals(post.getThreadId(), receivedPost.getThreadId());
        assertEquals(post.getPostNumber(), receivedPost.getPostNumber());
        assertEquals(post.getText(), receivedPost.getText());
        assertNotNull(receivedPost.getDateCreated());
        assertEquals(post.isHasTrophy(), receivedPost.isHasTrophy());
        assertEquals(post.getUpvotes(), receivedPost.getUpvotes());
        assertEquals(post.getDownvotes(), receivedPost.getDownvotes());
        assertEquals(post.getIsReported(), receivedPost.getIsReported());
    }

    @Test
    void postPost() throws Exception {
        // Create a thread
        ThreadModel thread = new ThreadModel();
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());

        documentReference.set(thread.generateMap()).get();
        threadsToDelete.add(thread.getId());

        // Create a post
        PostModel post = new PostModel(testPostModel);
        post.setThreadId(thread.getId());
        ObjectMapper mapper = new ObjectMapper();

        String postIdJson = mockMvc.perform(post("/post")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(post))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String postId = (String) mapper.readValue(postIdJson, HashMap.class).get("postId");
        postsToDelete.add(postId);

        DocumentSnapshot documentSnapshot = dbFirestore.collection(POSTS_COLLECTION_NAME).document(postId).get().get();
        PostModel createdPost = new PostModel(Objects.requireNonNull(documentSnapshot.getData()));

        assertEquals(postId, createdPost.getId());
        assertEquals(post.getUserEmail(), createdPost.getUserEmail());
        assertEquals(post.getThreadId(), createdPost.getThreadId());
        assertEquals(1, createdPost.getPostNumber());
        assertEquals(post.getText(), createdPost.getText());
        assertEquals(post.isHasTrophy(), createdPost.isHasTrophy());
        assertEquals(post.getUpvotes(), createdPost.getUpvotes());
        assertEquals(post.getDownvotes(), createdPost.getDownvotes());
        assertEquals(post.getIsReported(), createdPost.getIsReported());
    }

    @Test
    void putPost() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        PostModel post = new PostModel(testPostModel);
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();

        // Try to PUT non existing post
        mockMvc.perform(put("/post/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create a post
        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(documentReference.getId());

        PostModel putPost = new PostModel(post);
        putPost.setText(postControllerTestDataProperties.getPostPutText());

        // Try to update a field
        mockMvc.perform(put("/post/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putPost))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DocumentSnapshot documentSnapshot = dbFirestore.collection(POSTS_COLLECTION_NAME).document(documentReference.getId()).get().get();
        PostModel dbPost = new PostModel(Objects.requireNonNull(documentSnapshot.getData()));

        assertEquals(documentReference.getId(), dbPost.getId());
        assertEquals(post.getUserEmail(), dbPost.getUserEmail());
        assertEquals(post.getThreadId(), dbPost.getThreadId());
        assertEquals(post.getPostNumber(), dbPost.getPostNumber());
        assertEquals(postControllerTestDataProperties.getPostPutText(), dbPost.getText());
        assertEquals(post.isHasTrophy(), dbPost.isHasTrophy());
        assertEquals(post.getUpvotes(), dbPost.getUpvotes());
        assertEquals(post.getDownvotes(), dbPost.getDownvotes());
        assertEquals(post.getIsReported(), dbPost.getIsReported());

        // Try to update post that has an award
        documentReference.update("hasTrophy", true).get();

        mockMvc.perform(put("/post/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putPost))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    void deletePost() throws Exception {
        // Create a thread
        ThreadModel thread = new ThreadModel();
        DocumentReference threadDocumentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(threadDocumentReference.getId());

        threadDocumentReference.set(thread.generateMap()).get();
        threadsToDelete.add(thread.getId());

        PostModel post = new PostModel(testPostModel);
        DocumentReference postDocumentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();

        // Try to DELETE non existing post
        mockMvc.perform(delete("/post/" + postDocumentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create post
        post.setId(postDocumentReference.getId());
        post.setThreadId(thread.getId());
        post.setPostNumber(1L);
        postDocumentReference.set(post.generateMap()).get();

        postsToDelete.add(postDocumentReference.getId());

        // Try to delete post that has a trophy
        postDocumentReference.update("hasTrophy", true).get();

        mockMvc.perform(delete("/post/" + postDocumentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isConflict());

        postDocumentReference.update("hasTrophy", false).get();

        // DELETE post
        mockMvc.perform(delete("/post/" + postDocumentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        assertFalse(postDocumentReference.get().get().exists());

        // Add multiple posts to a thread
        int numPosts = 4;
        List<DocumentReference> postsDocumentReference = new ArrayList<>();
        for (int i = 0; i < numPosts; ++i) {
            postsDocumentReference.add(dbFirestore.collection(POSTS_COLLECTION_NAME).document());
            DocumentReference doc = postsDocumentReference.get(i);

            post.setPostNumber((long) (i + 1));
            post.setId(doc.getId());
            doc.set(post.generateMap()).get();

            postsToDelete.add(doc.getId());
        }

        // Delete a post and check that the postsNumber have changed
        int postToDeleteIndex = 1;

        mockMvc.perform(delete("/post/" + postsDocumentReference.get(postToDeleteIndex).getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());
        postsDocumentReference.remove(postToDeleteIndex);

        for (int i = 0; i < postsDocumentReference.size(); ++i) {
            PostModel p = new PostModel(Objects.requireNonNull(postsDocumentReference.get(i).get().get().getData()));
            assertEquals(i + 1, p.getPostNumber());
        }
    }

    @Test
    void getPostsByThreadId() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Create a thread
        ThreadModel thread = new ThreadModel();
        DocumentReference threadDocumentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(threadDocumentReference.getId());

        threadDocumentReference.set(thread.generateMap()).get();
        threadsToDelete.add(thread.getId());

        // Get posts for newly created thread
        String postsJson = mockMvc.perform(get("/post/postsByThreadId/" + thread.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PostModel> receivedPosts = mapper.readValue(postsJson, new TypeReference<>() {
        });

        assertEquals(0, receivedPosts.size());

        // Create some posts for the thread
        int numPosts = 3;

        for (int i = 0; i < numPosts; ++i) {
            PostModel post = new PostModel(testPostModel);
            post.setText(post.getText() + i);
            post.setThreadId(thread.getId());

            DocumentReference postDocumentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
            post.setId(postDocumentReference.getId());

            postDocumentReference.set(post.generateMap()).get();

            postsToDelete.add(post.getId());
        }

        // Get posts from thread
        postsJson = mockMvc.perform(get("/post/postsByThreadId/" + thread.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        receivedPosts = mapper.readValue(postsJson, new TypeReference<>() {
        });

        assertEquals(numPosts, receivedPosts.size());
    }

    @Test
    void upvotePost() throws Exception {
        // Upvote non existing post
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        mockMvc.perform(put("/post/" + documentReference.getId() + "/upvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new post
        PostModel post = new PostModel(testPostModel);
        post.setUserEmail(userTestDataProperties.getUserEmail());

        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(post.getId());

        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());

        // Set current user points just below the threshold of becoming a vip
        currentUserDocumentReference.update("vipStatus", false,
                "currentPoints", 999,
                "totalPoints", 999).get();

        UserModel currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Upvote the post
        mockMvc.perform(put("/post/" + post.getId() + "/upvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the current user has been added in the upvote list
        PostModel updatedPost = new PostModel(Objects.requireNonNull(documentReference.get().get().getData()));
        assertTrue(updatedPost.getUpvotes().contains(userTestDataProperties.getUserEmail()));

        // Check if the number of points has changed for the current user
        Long updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        Long updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() + 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() + 1, updatedTotalPoints.intValue());

        // Check that the user is now a vip
        Boolean updatedVipStatus = Objects.requireNonNull(currentUserDocumentReference.get().get().getBoolean("vipStatus"));
        assertTrue(updatedVipStatus);

        // Upvote again and check if the number of upvotes has changed
        mockMvc.perform(put("/post/" + post.getId() + "/upvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() + 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() + 1, updatedTotalPoints.intValue());

        // Remove upvote and add downvote directly in db
        updatedPost.getUpvotes().remove(userTestDataProperties.getUserEmail());
        updatedPost.getDownvotes().add(userTestDataProperties.getUserEmail());

        HashMap<String, Object> updatedVotes = new HashMap<>();
        updatedVotes.put("upvotes", Arrays.asList(updatedPost.getUpvotes().toArray()));
        updatedVotes.put("downvotes", Arrays.asList(updatedPost.getDownvotes().toArray()));

        documentReference.update(updatedVotes).get();

        // Get the current user again
        currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Upvote
        mockMvc.perform(put("/post/" + post.getId() + "/upvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the upvote was added and the downvote removed
        updatedPost = new PostModel(Objects.requireNonNull(documentReference.get().get().getData()));
        assertTrue(updatedPost.getUpvotes().contains(userTestDataProperties.getUserEmail()));
        assertFalse(updatedPost.getDownvotes().contains(userTestDataProperties.getUserEmail()));

        // Check if the points have been updated
        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() + 2, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() + 2, updatedTotalPoints.intValue());
    }

    @Test
    void downvotePost() throws Exception {
        // Downvote non existing post
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        mockMvc.perform(put("/post/" + documentReference.getId() + "/downvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new post
        PostModel post = new PostModel(testPostModel);
        post.setUserEmail(userTestDataProperties.getUserEmail());

        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(post.getId());

        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());
        UserModel currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Downvote the post
        mockMvc.perform(put("/post/" + post.getId() + "/downvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the current user has been added in the downvote list
        PostModel updatedPost = new PostModel(Objects.requireNonNull(documentReference.get().get().getData()));
        assertTrue(updatedPost.getDownvotes().contains(userTestDataProperties.getUserEmail()));

        // Check if the number of points has changed for the current user
        Long updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        Long updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() - 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() - 1, updatedTotalPoints.intValue());

        // Downvote again and check if the number of downvotes has changed
        mockMvc.perform(put("/post/" + post.getId() + "/downvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() - 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() - 1, updatedTotalPoints.intValue());

        // Remove downvote and add upvote directly in db
        updatedPost.getDownvotes().remove(userTestDataProperties.getUserEmail());
        updatedPost.getUpvotes().add(userTestDataProperties.getUserEmail());

        HashMap<String, Object> updatedVotes = new HashMap<>();
        updatedVotes.put("upvotes", Arrays.asList(updatedPost.getUpvotes().toArray()));
        updatedVotes.put("downvotes", Arrays.asList(updatedPost.getDownvotes().toArray()));

        documentReference.update(updatedVotes).get();

        // Get the current user again
        currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Downvote
        mockMvc.perform(put("/post/" + post.getId() + "/downvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the downvote was added and the upvote removed
        updatedPost = new PostModel(Objects.requireNonNull(documentReference.get().get().getData()));
        assertTrue(updatedPost.getDownvotes().contains(userTestDataProperties.getUserEmail()));
        assertFalse(updatedPost.getUpvotes().contains(userTestDataProperties.getUserEmail()));

        // Check if the points have been updated
        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() - 2, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() - 2, updatedTotalPoints.intValue());
    }

    @Test
    void removeUpvotePost() throws Exception {
        // Remove upvote from non existing post
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeUpvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new post and add current user to the upvotes list
        PostModel post = new PostModel(testPostModel);
        post.setUserEmail(userTestDataProperties.getUserEmail());
        post.getUpvotes().add(userTestDataProperties.getUserEmail());

        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(post.getId());

        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());
        UserModel currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Remove upvote
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeUpvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the current user has disappeared from the upvote list
        List<String> upvotesList = (List<String>) documentReference.get().get().get("upvotes");
        assertFalse(Objects.requireNonNull(upvotesList).contains(userTestDataProperties.getUserEmail()));

        // Check if the user's points have been updated
        Long updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        Long updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() - 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() - 1, updatedTotalPoints.intValue());

        // Try to remove the upvote again
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeUpvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the number of points has changed
        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() - 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() - 1, updatedTotalPoints.intValue());
    }

    @Test
    void removeDownvotePost() throws Exception {
        // Remove downvote from non existing post
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeDownvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new post and add current user to the upvotes list
        PostModel post = new PostModel(testPostModel);
        post.setUserEmail(userTestDataProperties.getUserEmail());
        post.getDownvotes().add(userTestDataProperties.getUserEmail());

        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(post.getId());

        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());
        UserModel currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Remove downvote
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeDownvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the current user has disappeared from the upvote list
        List<String> downvotesList = (List<String>) documentReference.get().get().get("downvotes");
        assertFalse(Objects.requireNonNull(downvotesList).contains(userTestDataProperties.getUserEmail()));

        // Check if the user's points have been updated
        Long updatedCurrentPoints = (Long) Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        Long updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() + 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() + 1, updatedTotalPoints.intValue());

        // Try to remove the downvote again
        mockMvc.perform(put("/post/" + documentReference.getId() + "/removeDownvote")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the number of points has changed
        updatedCurrentPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("currentPoints"));
        updatedTotalPoints = Objects.requireNonNull(currentUserDocumentReference.get().get().getLong("totalPoints"));

        assertEquals(currentUser.getCurrentPoints() + 1, updatedCurrentPoints.intValue());
        assertEquals(currentUser.getTotalPoints() + 1, updatedTotalPoints.intValue());
    }

    @Test
    void awardTrophy() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        // Try to award trophy to non existing post
        DocumentReference documentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
        mockMvc.perform(put("/post/" + documentReference.getId() + "/awardTrophy")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new thread
        ThreadModel thread = new ThreadModel();
        thread.setHasTrophy(false);
        thread.setVipStatus(false);

        DocumentReference threadDocumentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(threadDocumentReference.getId());
        threadDocumentReference.set(thread.generateMap()).get();

        threadsToDelete.add(thread.getId());

        // Create post for thread
        PostModel post = new PostModel(testPostModel);
        post.setUserEmail(userTestDataProperties.getUserEmail());
        post.setThreadId(thread.getId());
        post.setHasTrophy(false);

        post.setId(documentReference.getId());
        documentReference.set(post.generateMap()).get();

        postsToDelete.add(post.getId());

        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());

        // Set current user points just below the threshold of becoming a vip
        currentUserDocumentReference.update("vipStatus", false,
                "currentPoints", 999,
                "totalPoints", 999,
                "trophies", 0).get();

        UserModel currentUser = new UserModel(Objects.requireNonNull(currentUserDocumentReference.get().get().getData()));

        // Award thread
        mockMvc.perform(put("/post/" + documentReference.getId() + "/awardTrophy")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the post has received the award
        assertEquals(true, documentReference.get().get().getBoolean("hasTrophy"));

        // Check if the thread has updated the hasTrophy flag
        assertEquals(true, threadDocumentReference.get().get().getBoolean("hasTrophy"));

        // Check the user's trophies
        Long updatedUserTrophies = currentUserDocumentReference.get().get().getLong("trophies");
        assertEquals(currentUser.getTrophies() + 1, updatedUserTrophies);

        // Check that the user is now a vip
        Boolean updatedVipStatus = Objects.requireNonNull(currentUserDocumentReference.get().get().getBoolean("vipStatus"));
        assertTrue(updatedVipStatus);

        // Try to award again
        mockMvc.perform(put("/post/" + documentReference.getId() + "/awardTrophy")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if the user's trophies have been updated
        updatedUserTrophies = currentUserDocumentReference.get().get().getLong("trophies");
        assertEquals(currentUser.getTrophies() + 1, updatedUserTrophies);
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).delete().get();

        for (String id : threadsToDelete)
            dbFirestore.collection(THREADS_COLLECTION_NAME).document(id).delete().get();

        for (String id : postsToDelete)
            dbFirestore.collection(POSTS_COLLECTION_NAME).document(id).delete().get();
    }
}
