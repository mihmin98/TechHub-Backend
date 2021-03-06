package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.PostControllerTestDataProperties;
import com.techflow.techhubbackend.config.ThreadCategoriesProperties;
import com.techflow.techhubbackend.config.ThreadControllerTestDataProperties;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
import com.techflow.techhubbackend.model.UserModel;
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

import java.security.SecureRandom;
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
public class ThreadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private ThreadControllerTestDataProperties threadControllerTestDataProperties;

    @Autowired
    private PostControllerTestDataProperties postControllerTestDataProperties;

    @Autowired
    private ThreadCategoriesProperties threadCategoriesProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private String regularJwt = null;
    private String vipJwt = null;

    private ThreadModel testThreadModel;

    private final List<String> threadsToDelete = new ArrayList<>();
    private final List<String> postsToDelete = new ArrayList<>();

    private final String USER_COLLECTION_NAME = "user";
    private final String THREADS_COLLECTION_NAME = "thread";
    private final String POSTS_COLLECTION_NAME = "post";

    @BeforeAll
    void login() throws Exception {
        testThreadModel = new ThreadModel(null, threadControllerTestDataProperties.getThreadOwnerEmail(), threadControllerTestDataProperties.getThreadTitle(), threadControllerTestDataProperties.getThreadCategory(), threadControllerTestDataProperties.getThreadText(), null, threadControllerTestDataProperties.getHasTrophy(), threadControllerTestDataProperties.getVipStatus(), threadControllerTestDataProperties.getIsReported(), threadControllerTestDataProperties.getIsLocked());

        // Create non-vip user
        UserModel user = new UserModel(userTestDataProperties.getUserEmail(), userTestDataProperties.getUserPassword(), userTestDataProperties.getUserUsername(), userTestDataProperties.getUserType(), userTestDataProperties.getUserProfilePicture(), userTestDataProperties.getUserAccountStatus());

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
        regularJwt = obj.get("accessToken");

        // Create vip user
        UserModel vipUser = new UserModel(userTestDataProperties.getUserVipEmail(), userTestDataProperties.getUserPassword(), userTestDataProperties.getUserUsername(), userTestDataProperties.getUserVipType(), userTestDataProperties.getUserProfilePicture(), userTestDataProperties.getUserAccountStatus());
        vipUser.setVipStatus(userTestDataProperties.getUserVipVipStatus());

        vipUser.setPassword(bCryptPasswordEncoder.encode(vipUser.getPassword()));
        dbFirestore.collection(USER_COLLECTION_NAME).document(vipUser.getEmail()).set(vipUser.generateMap()).get();
        vipUser.setPassword(userTestDataProperties.getUserPassword());

        // Login vip user
        mapper = new ObjectMapper();
        node = mapper.createObjectNode();
        node.put("email", vipUser.getEmail());
        node.put("password", vipUser.getPassword());
        testUserLoginJson = mapper.writeValueAsString(node);

        loginResult = mockMvc.perform(post("/login")
                .content(testUserLoginJson).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        obj = mapper.readValue(loginResult, HashMap.class);
        vipJwt = obj.get("accessToken");
    }

    @Test
    void getAllThreads() throws Exception {
        // Create a thread
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Get all threads
        String threadsJson = mockMvc.perform(get("/thread")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<ThreadModel> threads = mapper.readValue(threadsJson, new TypeReference<>() {
        });

        boolean foundThread = false;

        for (ThreadModel t : threads) {
            if (t.getId().equals(documentReference.getId())) {
                foundThread = true;
                break;
            }
        }

        assertTrue(threads.size() > 0);
        assertTrue(foundThread);
    }

    @Test
    void getThread() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to get non existing thread
        mockMvc.perform(get("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Get thread
        String threadJson = mockMvc.perform(get("/thread/" + thread.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        ThreadModel getResult = mapper.readValue(threadJson, ThreadModel.class);

        assertEquals(thread.getId(), getResult.getId());
        assertEquals(thread.getOwnerEmail(), getResult.getOwnerEmail());
        assertEquals(thread.getTitle(), getResult.getTitle());
        assertEquals(thread.getCategory(), getResult.getCategory());
        assertEquals(thread.getText(), getResult.getText());
        assertEquals(thread.getHasTrophy(), getResult.getHasTrophy());
        assertEquals(thread.getVipStatus(), getResult.getVipStatus());
        assertEquals(thread.getIsReported(), getResult.getIsReported());
        assertEquals(thread.getIsLocked(), getResult.getIsLocked());
        assertNotNull(getResult.getDateCreated());
    }

    @Test
    void postThread() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        ObjectMapper mapper = new ObjectMapper();

        String threadIdJson = mockMvc.perform(post("/thread")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content(mapper.writeValueAsString(testThreadModel))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String threadId = (String) mapper.readValue(threadIdJson, HashMap.class).get("threadId");
        threadsToDelete.add(threadId);

        DocumentSnapshot document = dbFirestore.collection(THREADS_COLLECTION_NAME).document(threadId).get().get();
        ThreadModel createdThread = new ThreadModel(Objects.requireNonNull(document.getData()));

        assertEquals(threadId, createdThread.getId());
        assertEquals(thread.getOwnerEmail(), createdThread.getOwnerEmail());
        assertEquals(thread.getTitle(), createdThread.getTitle());
        assertEquals(thread.getCategory(), createdThread.getCategory());
        assertEquals(thread.getText(), createdThread.getText());
        assertEquals(false, createdThread.getHasTrophy());
        assertEquals(false, createdThread.getVipStatus());
        assertEquals(false, createdThread.getIsReported());
        assertEquals(false, createdThread.getIsLocked());
    }

    @Test
    void putThread() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to PUT non existing thread
        mockMvc.perform(put("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        ThreadModel putThread = new ThreadModel();
        putThread.setText(threadControllerTestDataProperties.getThreadPutText());

        // Try to update a field
        mockMvc.perform(put("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content(mapper.writeValueAsString(putThread))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DocumentSnapshot document = dbFirestore.collection(THREADS_COLLECTION_NAME).document(documentReference.getId()).get().get();
        ThreadModel dbThread = new ThreadModel(Objects.requireNonNull(document.getData()));

        assertEquals(documentReference.getId(), dbThread.getId());
        assertEquals(thread.getOwnerEmail(), dbThread.getOwnerEmail());
        assertEquals(thread.getTitle(), dbThread.getTitle());
        assertEquals(thread.getCategory(), dbThread.getCategory());
        assertEquals(thread.getHasTrophy(), dbThread.getHasTrophy());
        assertEquals(thread.getVipStatus(), dbThread.getVipStatus());
        assertEquals(thread.getIsReported(), dbThread.getIsReported());
        assertEquals(thread.getIsLocked(), dbThread.getIsLocked());
        assertEquals(threadControllerTestDataProperties.getThreadPutText(), dbThread.getText());
    }

    @Test
    void deleteThread() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to DELETE non existing thread
        mockMvc.perform(delete("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // DELETE thread
        mockMvc.perform(delete("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk());

        assertFalse(documentReference.get().get().exists());
    }

    @Test
    void getCategories() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        String categoriesJson = mockMvc.perform(get("/thread/categories")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<String> receivedCategories = mapper.readValue(categoriesJson, List.class);

        assertTrue(threadCategoriesProperties.getCategories().containsAll(receivedCategories));
    }

    @Test
    void getThreadsByCategories() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        thread.setCategory(threadCategoriesProperties.getCategories().get(0));
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Get all threads
        String threadsJson = mockMvc.perform(get("/thread/categories/" + threadCategoriesProperties.getCategories().get(0))
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<ThreadModel> threads = mapper.readValue(threadsJson, new TypeReference<>() {
        });

        boolean foundThread = false;

        for (ThreadModel t : threads) {
            if (t.getId().equals(documentReference.getId())) {
                foundThread = true;
                break;
            }
        }

        assertTrue(threads.size() > 0);
        assertTrue(foundThread);
    }

    @Test
    void getThreadNumberOfPosts() throws Exception {
        long receivedNumOfPosts;
        ObjectMapper mapper = new ObjectMapper();

        // Create a thread
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());

        threadsToDelete.add(documentReference.getId());

        mockMvc.perform(get("/thread/" + documentReference.getId() + "/postsCount")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isNotFound());

        documentReference.set(thread.generateMap()).get();

        receivedNumOfPosts = Long.parseLong(mockMvc.perform(get("/thread/" + documentReference.getId() + "/postsCount")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertEquals(0, receivedNumOfPosts);

        // Create a few posts
        int numOfPosts = 3;
        for (int i = 0; i < numOfPosts; ++i) {
            PostModel post = new PostModel(null, thread.getOwnerEmail(), thread.getId(), null, postControllerTestDataProperties.getPostText(), null, postControllerTestDataProperties.getPostHasTrophy(), postControllerTestDataProperties.getUpvotes(), postControllerTestDataProperties.getDownvotes(), postControllerTestDataProperties.getIsReported());

            DocumentReference postDocumentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
            post.setId(postDocumentReference.getId());
            post.setThreadId(thread.getId());

            postsToDelete.add(postDocumentReference.getId());

            postDocumentReference.set(post.generateMap()).get();
        }

        receivedNumOfPosts = Long.parseLong(mockMvc.perform(get("/thread/" + documentReference.getId() + "/postsCount")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString());

        assertEquals(numOfPosts, receivedNumOfPosts);
    }

    @Test
    void getThreadPosts() throws Exception {
        String receivedPostsJson;
        ObjectMapper mapper = new ObjectMapper();

        // Create a thread
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());

        threadsToDelete.add(documentReference.getId());

        mockMvc.perform(get("/thread/" + documentReference.getId() + "/posts")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isNotFound());

        documentReference.set(thread.generateMap()).get();

        receivedPostsJson = mockMvc.perform(get("/thread/" + documentReference.getId() + "/posts")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<PostModel> receivedPosts = mapper.readValue(receivedPostsJson, new TypeReference<>() {
        });

        assertEquals(0, receivedPosts.size());

        // Create some posts
        int numOfPosts = 3;
        for (int i = 0; i < numOfPosts; ++i) {
            PostModel post = new PostModel(null, thread.getOwnerEmail(), thread.getId(), null, postControllerTestDataProperties.getPostText(), null, postControllerTestDataProperties.getPostHasTrophy(), postControllerTestDataProperties.getUpvotes(), postControllerTestDataProperties.getDownvotes(), postControllerTestDataProperties.getIsReported());

            DocumentReference postDocumentReference = dbFirestore.collection(POSTS_COLLECTION_NAME).document();
            post.setId(postDocumentReference.getId());
            post.setThreadId(thread.getId());
            post.setPostNumber((long) (i + 1));

            postsToDelete.add(postDocumentReference.getId());

            postDocumentReference.set(post.generateMap()).get();
        }

        receivedPostsJson = mockMvc.perform(get("/thread/" + documentReference.getId() + "/posts")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        receivedPosts = mapper.readValue(receivedPostsJson, new TypeReference<>() {
        });

        assertEquals(numOfPosts, receivedPosts.size());
        for (int i = 0; i < numOfPosts; ++i) {
            assertEquals(postControllerTestDataProperties.getPostUserEmail(), receivedPosts.get(i).getUserEmail());
            assertEquals(thread.getId(), receivedPosts.get(i).getThreadId());
            assertEquals(i + 1, receivedPosts.get(i).getPostNumber());
            assertEquals(postControllerTestDataProperties.getPostText(), receivedPosts.get(i).getText());
            assertEquals(postControllerTestDataProperties.getPostHasTrophy(), receivedPosts.get(i).isHasTrophy());
            assertEquals(postControllerTestDataProperties.getUpvotes(), receivedPosts.get(i).getUpvotes());
            assertEquals(postControllerTestDataProperties.getDownvotes(), receivedPosts.get(i).getDownvotes());
        }
    }

    @Test
    void getThreadsByTitle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();
        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Generate a random string that will be the search term
        int maxRandom = alphanumeric.length();

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 20; ++i) {
            builder.append(alphanumeric.charAt(random.nextInt(maxRandom)));
        }

        String randomString = builder.toString();

        // Try to search for nonexistent string
        String responseJson = mockMvc.perform(get("/thread/title/" + randomString)
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ThreadModel> receivedThreads = mapper.readValue(responseJson, new TypeReference<>() {
        });

        assertEquals(0, receivedThreads.size());

        // Create document
        DocumentReference document = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        ThreadModel thread = new ThreadModel(testThreadModel);
        thread.setTitle(randomString);
        thread.setId(document.getId());

        document.set(thread.generateMap()).get();

        threadsToDelete.add(document.getId());

        // Try to search for created thread
        responseJson = mockMvc.perform(get("/thread/title/" + randomString)
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        receivedThreads = mapper.readValue(responseJson, new TypeReference<>() {
        });

        assertEquals(1, receivedThreads.size());
        ThreadModel receivedThread = receivedThreads.get(0);

        assertEquals(document.getId(), receivedThread.getId());
        assertEquals(thread.getOwnerEmail(), receivedThread.getOwnerEmail());
        assertEquals(thread.getTitle(), receivedThread.getTitle());
        assertEquals(thread.getCategory(), receivedThread.getCategory());
        assertEquals(thread.getText(), receivedThread.getText());
        assertEquals(thread.getHasTrophy(), receivedThread.getHasTrophy());
        assertEquals(thread.getVipStatus(), receivedThread.getVipStatus());
        assertEquals(thread.getIsReported(), receivedThread.getIsReported());
    }

    @Test
    void getVIPThreadsByTitle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        StringBuilder builder = new StringBuilder();
        String alphanumeric = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        // Generate a random string that will be the search term
        int maxRandom = alphanumeric.length();

        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 20; ++i) {
            builder.append(alphanumeric.charAt(random.nextInt(maxRandom)));
        }

        String randomString = builder.toString();

        // Call endpoint with non vip user
        mockMvc.perform(get("/thread/vip/title/" + randomString)
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isUnauthorized());

        // Try to search for nonexistent string
        String responseJson = mockMvc.perform(get("/thread/vip/title/" + randomString)
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ThreadModel> receivedThreads = mapper.readValue(responseJson, new TypeReference<>() {
        });

        assertEquals(0, receivedThreads.size());

        // Create document
        DocumentReference document = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        ThreadModel thread = new ThreadModel(testThreadModel);
        thread.setTitle(randomString);
        thread.setId(document.getId());
        thread.setVipStatus(true);

        document.set(thread.generateMap()).get();

        threadsToDelete.add(document.getId());

        // Try to search for created thread
        responseJson = mockMvc.perform(get("/thread/vip/title/" + randomString)
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        receivedThreads = mapper.readValue(responseJson, new TypeReference<>() {
        });

        assertEquals(1, receivedThreads.size());
        ThreadModel receivedThread = receivedThreads.get(0);

        assertEquals(document.getId(), receivedThread.getId());
        assertEquals(thread.getOwnerEmail(), receivedThread.getOwnerEmail());
        assertEquals(thread.getTitle(), receivedThread.getTitle());
        assertEquals(thread.getCategory(), receivedThread.getCategory());
        assertEquals(thread.getText(), receivedThread.getText());
        assertEquals(thread.getHasTrophy(), receivedThread.getHasTrophy());
        assertEquals(thread.getVipStatus(), receivedThread.getVipStatus());
        assertEquals(thread.getIsReported(), receivedThread.getIsReported());
    }

    @Test
    void getAllVIPThreads() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Call endpoint with non vip user
        mockMvc.perform(get("/thread/vip")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isUnauthorized());

        // Create a vip thread
        ThreadModel thread = new ThreadModel(testThreadModel);
        thread.setVipStatus(true);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Get all vip threads
        String threadsJson = mockMvc.perform(get("/thread/vip")
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ThreadModel> threads = mapper.readValue(threadsJson, new TypeReference<>() {
        });

        boolean foundThread = false;

        for (ThreadModel t : threads) {
            if (t.getId().equals(documentReference.getId())) {
                foundThread = true;
                break;
            }
        }

        assertTrue(threads.size() > 0);
        assertTrue(foundThread);
    }

    @Test
    void getVIPThreadsByCategory() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        // Call endpoint with non vip user
        mockMvc.perform(get("/thread/vip/categories/" + threadCategoriesProperties.getCategories().get(0))
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isUnauthorized());

        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        thread.setCategory(threadCategoriesProperties.getCategories().get(0));
        thread.setVipStatus(true);
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Get all threads
        String threadsJson = mockMvc.perform(get("/thread/vip/categories/" + threadCategoriesProperties.getCategories().get(0))
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<ThreadModel> threads = mapper.readValue(threadsJson, new TypeReference<>() {
        });

        boolean foundThread = false;

        for (ThreadModel t : threads) {
            if (t.getId().equals(documentReference.getId())) {
                foundThread = true;
                break;
            }
        }

        assertTrue(threads.size() > 0);
        assertTrue(foundThread);
    }

    @Test
    void getVIPThread() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        ThreadModel thread = new ThreadModel(testThreadModel);
        thread.setVipStatus(true);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to get non existing thread
        mockMvc.perform(get("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Try to get thread with non vip user
        mockMvc.perform(get("/thread/" + thread.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isUnauthorized());

        // Get thread
        String threadJson = mockMvc.perform(get("/thread/" + thread.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();


        ThreadModel getResult = mapper.readValue(threadJson, ThreadModel.class);

        assertEquals(thread.getId(), getResult.getId());
        assertEquals(thread.getOwnerEmail(), getResult.getOwnerEmail());
        assertEquals(thread.getTitle(), getResult.getTitle());
        assertEquals(thread.getCategory(), getResult.getCategory());
        assertEquals(thread.getText(), getResult.getText());
        assertEquals(thread.getHasTrophy(), getResult.getHasTrophy());
        assertEquals(thread.getVipStatus(), getResult.getVipStatus());
        assertEquals(thread.getIsReported(), getResult.getIsReported());
        assertNotNull(getResult.getDateCreated());
    }

    @Test
    void createVIPThread() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        ObjectMapper mapper = new ObjectMapper();

        // Try to POST thread with non vip user
        mockMvc.perform(post("/thread/vip")
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content(mapper.writeValueAsString(testThreadModel))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // POST thread
        String threadIdJson = mockMvc.perform(post("/thread/vip")
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt)
                .content(mapper.writeValueAsString(testThreadModel))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String threadId = (String) mapper.readValue(threadIdJson, HashMap.class).get("threadId");
        threadsToDelete.add(threadId);

        DocumentSnapshot document = dbFirestore.collection(THREADS_COLLECTION_NAME).document(threadId).get().get();
        ThreadModel createdThread = new ThreadModel(Objects.requireNonNull(document.getData()));

        assertEquals(threadId, createdThread.getId());
        assertEquals(thread.getOwnerEmail(), createdThread.getOwnerEmail());
        assertEquals(thread.getTitle(), createdThread.getTitle());
        assertEquals(thread.getCategory(), createdThread.getCategory());
        assertEquals(thread.getText(), createdThread.getText());
        assertEquals(false, createdThread.getHasTrophy());
        assertEquals(true, createdThread.getVipStatus());
        assertEquals(false, createdThread.getIsReported());
    }

    @Test
    void putVIPThread() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ThreadModel thread = new ThreadModel(testThreadModel);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to PUT non existing thread
        mockMvc.perform(put("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        thread.setVipStatus(true);
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        ThreadModel putThread = new ThreadModel();
        putThread.setText(threadControllerTestDataProperties.getThreadPutText());

        // Try to update a field as a non vip user
        mockMvc.perform(put("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt)
                .content(mapper.writeValueAsString(putThread))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        // Try to update a field
        mockMvc.perform(put("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt)
                .content(mapper.writeValueAsString(putThread))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DocumentSnapshot document = dbFirestore.collection(THREADS_COLLECTION_NAME).document(documentReference.getId()).get().get();
        ThreadModel dbThread = new ThreadModel(Objects.requireNonNull(document.getData()));

        assertEquals(documentReference.getId(), dbThread.getId());
        assertEquals(thread.getOwnerEmail(), dbThread.getOwnerEmail());
        assertEquals(thread.getTitle(), dbThread.getTitle());
        assertEquals(thread.getCategory(), dbThread.getCategory());
        assertEquals(thread.getHasTrophy(), dbThread.getHasTrophy());
        assertEquals(thread.getVipStatus(), dbThread.getVipStatus());
        assertEquals(thread.getIsReported(), dbThread.getIsReported());
        assertEquals(threadControllerTestDataProperties.getThreadPutText(), dbThread.getText());
    }

    @Test
    void deleteVIPThread() throws Exception {
        ThreadModel thread = new ThreadModel(testThreadModel);
        thread.setVipStatus(true);
        DocumentReference documentReference = dbFirestore.collection(THREADS_COLLECTION_NAME).document();

        // Try to DELETE non existing thread
        mockMvc.perform(delete("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isNotFound());

        // Create a thread
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        threadsToDelete.add(documentReference.getId());

        // Try to DELETE thread as non vip user
        mockMvc.perform(delete("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, regularJwt))
                .andExpect(status().isUnauthorized());

        // DELETE thread
        mockMvc.perform(delete("/thread/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, vipJwt))
                .andExpect(status().isOk());

        assertFalse(documentReference.get().get().exists());
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).delete().get();
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserVipEmail()).delete().get();

        for (String id : threadsToDelete)
            dbFirestore.collection(THREADS_COLLECTION_NAME).document(id).delete().get();

        for (String id : postsToDelete)
            dbFirestore.collection(POSTS_COLLECTION_NAME).document(id).delete().get();
    }
}
