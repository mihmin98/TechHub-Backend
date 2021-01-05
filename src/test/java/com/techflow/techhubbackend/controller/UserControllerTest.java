package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
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

import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties testDataProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private String jwt = null;

    private UserModel testUser;

    private final String COLLECTION_NAME = "user";

    @BeforeAll
    void login() throws Exception {
        testUser = new UserModel(testDataProperties.getUserEmail(), testDataProperties.getUserPassword(), testDataProperties.getUserUsername(), testDataProperties.getUserType(), testDataProperties.getUserProfilePicture(), testDataProperties.getUserAccountStatus());

        testUser.setPassword(bCryptPasswordEncoder.encode(testUser.getPassword()));
        dbFirestore.collection(COLLECTION_NAME).document(testUser.getEmail()).set(testUser.generateMap()).get();
        testUser.setPassword(testDataProperties.getUserPassword());

        // Login
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("email", testUser.getEmail());
        node.put("password", testUser.getPassword());
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
    void getUser() throws Exception {
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserInvalidEmail()).delete().get();

        mockMvc.perform(get("/user/" + testDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        String userJson = mockMvc.perform(get("/user/" + testUser.getEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserModel getResult = new UserModel((HashMap<String, Object>) mapper.readValue(userJson, HashMap.class));

        assertEquals(testUser.getEmail(), getResult.getEmail());
        assertEquals(testUser.getUsername(), getResult.getUsername());
        assertTrue(getResult.getPassword().isEmpty());
        assertEquals(testUser.getType(), getResult.getType());
        assertEquals(testUser.getProfilePicture(), getResult.getProfilePicture());
        assertEquals(testUser.getAccountStatus(), getResult.getAccountStatus());
    }

    @Test
    void postUser() throws Exception {
        UserModel postUser = new UserModel(testUser);
        postUser.setEmail(testDataProperties.getUserPostEmail());

        // Make sure user does not exist
        dbFirestore.collection(COLLECTION_NAME).document(postUser.getEmail()).delete().get();

        ObjectMapper objectMapper = new ObjectMapper();

        // POST new user
        mockMvc.perform(post("/user")
                .content(objectMapper.writeValueAsString(postUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Try to POST same user
        mockMvc.perform(post("/user")
                .content(objectMapper.writeValueAsString(postUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());

        DocumentSnapshot document = dbFirestore.collection(COLLECTION_NAME).document(postUser.getEmail()).get().get();
        UserModel resultUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(postUser.getEmail(), resultUser.getEmail());
        assertEquals(postUser.getUsername(), resultUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(testUser.getPassword(), resultUser.getPassword()));
        assertEquals(postUser.getType(), resultUser.getType());
        assertEquals(postUser.getProfilePicture(), resultUser.getProfilePicture());
        assertEquals(postUser.getAccountStatus(), resultUser.getAccountStatus());
    }

    @Test
    void deleteUser() throws Exception {
        // Try to delete user that does not exist
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserInvalidEmail()).delete().get();

        mockMvc.perform(delete("/user/" + testDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create user then delete him
        UserModel deleteUser = new UserModel(testUser);
        deleteUser.setEmail(testDataProperties.getUserDeleteEmail());
        dbFirestore.collection(COLLECTION_NAME).document(deleteUser.getEmail()).set(deleteUser.generateMap()).get();

        mockMvc.perform(delete("/user/" + deleteUser.getEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check if deleted document exists
        DocumentSnapshot document = dbFirestore.collection(COLLECTION_NAME).document(deleteUser.getEmail()).get().get();
        assertFalse(document.exists());
    }

    @Test
    void putUser() throws Exception {
        // Delete invalid user
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserInvalidEmail()).delete().get();

        // Try to PUT on non existing user
        mockMvc.perform(put("/user/" + testDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create PUT user
        UserModel putUser = new UserModel(testUser);
        putUser.setEmail(testDataProperties.getUserPutInitialEmail());
        putUser.setPassword(bCryptPasswordEncoder.encode(putUser.getPassword()));

        dbFirestore.collection(COLLECTION_NAME).document(putUser.getEmail()).set(putUser.generateMap()).get();

        // Try to update a field
        putUser = new UserModel();
        putUser.setAccountStatus(testDataProperties.getUserPutChangedAccountStatus());
        ObjectMapper mapper = new ObjectMapper();

        mockMvc.perform(put("/user/" + testDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DocumentSnapshot document = dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutInitialEmail()).get().get();
        UserModel dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(testDataProperties.getUserPutInitialEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(testDataProperties.getUserPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(putUser.getAccountStatus(), dbUser.getAccountStatus());

        // Try to update password
        putUser = new UserModel();
        putUser.setPassword(testDataProperties.getUserPutChangedPassword());

        mockMvc.perform(put("/user/" + testDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        document = dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutInitialEmail()).get().get();
        dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(testDataProperties.getUserPutInitialEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(testDataProperties.getUserPutChangedPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(testDataProperties.getUserPutChangedAccountStatus(), dbUser.getAccountStatus());

        // Try to change email
        putUser = new UserModel();
        putUser.setEmail(testDataProperties.getUserPutChangedEmail());

        mockMvc.perform(put("/user/" + testDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        document = dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutInitialEmail()).get().get();
        assertFalse(document.exists());

        document = dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutChangedEmail()).get().get();
        dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(testDataProperties.getUserPutChangedEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(testDataProperties.getUserPutChangedPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(testDataProperties.getUserPutChangedAccountStatus(), dbUser.getAccountStatus());
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(COLLECTION_NAME).document(testUser.getEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPostEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutInitialEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(testDataProperties.getUserPutChangedEmail()).delete().get();
    }
}
