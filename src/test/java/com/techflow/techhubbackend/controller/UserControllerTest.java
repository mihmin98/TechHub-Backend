package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    private String jwt = null;

    private UserModel testUser;

    private final String COLLECTION_NAME = "user";

    private final List<UserModel> usersToDelete = new ArrayList<>();

    @BeforeAll
    void login() throws Exception {
        testUser = new UserModel(userTestDataProperties.getUserEmail(),
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

        testUser.setPassword(bCryptPasswordEncoder.encode(testUser.getPassword()));
        dbFirestore.collection(COLLECTION_NAME).document(testUser.getEmail()).set(testUser.generateMap()).get();
        testUser.setPassword(userTestDataProperties.getUserPassword());

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
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserInvalidEmail()).delete().get();

        mockMvc.perform(get("/user/" + userTestDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        String userJson = mockMvc.perform(get("/user/" + testUser.getEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UserModel getResult = getUserFromJSON(userJson);

        assertEquals(testUser.getEmail(), getResult.getEmail());
        assertEquals(testUser.getUsername(), getResult.getUsername());
        assertTrue(getResult.getPassword().isEmpty());
        assertEquals(testUser.getType(), getResult.getType());
        assertEquals(testUser.getProfilePicture(), getResult.getProfilePicture());
        assertEquals(testUser.getAccountStatus(), getResult.getAccountStatus());
        assertEquals(testUser.getTotalPoints(), getResult.getTotalPoints());
        assertEquals(testUser.getCurrentPoints(), getResult.getCurrentPoints());
        assertEquals(testUser.getTrophies(), getResult.getTrophies());
        assertEquals(testUser.isVipStatus(), getResult.isVipStatus());
        assertEquals(testUser.getRafflesWon(), getResult.getRafflesWon());
    }

    @Test
    void postUser() throws Exception {
        UserModel postUser = new UserModel(testUser);
        postUser.setEmail(userTestDataProperties.getUserPostEmail());

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
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserInvalidEmail()).delete().get();

        mockMvc.perform(delete("/user/" + userTestDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create user then delete him
        UserModel deleteUser = new UserModel(testUser);
        deleteUser.setEmail(userTestDataProperties.getUserDeleteEmail());
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
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserInvalidEmail()).delete().get();

        // Try to PUT on non existing user
        mockMvc.perform(put("/user/" + userTestDataProperties.getUserInvalidEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Create PUT user
        UserModel putUser = new UserModel(testUser);
        putUser.setEmail(userTestDataProperties.getUserPutInitialEmail());
        putUser.setPassword(bCryptPasswordEncoder.encode(putUser.getPassword()));

        dbFirestore.collection(COLLECTION_NAME).document(putUser.getEmail()).set(putUser.generateMap()).get();

        // Try to update a field
        putUser = new UserModel();
        putUser.setAccountStatus(userTestDataProperties.getUserPutChangedAccountStatus());
        ObjectMapper mapper = new ObjectMapper();

        mockMvc.perform(put("/user/" + userTestDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        DocumentSnapshot document = dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutInitialEmail()).get().get();
        UserModel dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(userTestDataProperties.getUserPutInitialEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(userTestDataProperties.getUserPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(putUser.getAccountStatus(), dbUser.getAccountStatus());

        // Try to update password
        putUser = new UserModel();
        putUser.setPassword(userTestDataProperties.getUserPutChangedPassword());

        mockMvc.perform(put("/user/" + userTestDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        document = dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutInitialEmail()).get().get();
        dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(userTestDataProperties.getUserPutInitialEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(userTestDataProperties.getUserPutChangedPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(userTestDataProperties.getUserPutChangedAccountStatus(), dbUser.getAccountStatus());

        // Try to change email
        putUser = new UserModel();
        putUser.setEmail(userTestDataProperties.getUserPutChangedEmail());

        mockMvc.perform(put("/user/" + userTestDataProperties.getUserPutInitialEmail())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt)
                .content(mapper.writeValueAsString(putUser))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        document = dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutInitialEmail()).get().get();
        assertFalse(document.exists());

        document = dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutChangedEmail()).get().get();
        dbUser = new UserModel(Objects.requireNonNull(document.getData()));

        assertEquals(userTestDataProperties.getUserPutChangedEmail(), dbUser.getEmail());
        assertEquals(testUser.getUsername(), dbUser.getUsername());
        assertTrue(bCryptPasswordEncoder.matches(userTestDataProperties.getUserPutChangedPassword(), dbUser.getPassword()));
        assertEquals(testUser.getType(), dbUser.getType());
        assertEquals(testUser.getProfilePicture(), dbUser.getProfilePicture());
        assertEquals(userTestDataProperties.getUserPutChangedAccountStatus(), dbUser.getAccountStatus());
    }

    @Test
    void sortUsersByPointsAndTrophies() throws Exception {
        assertEquals(userTestDataProperties.getUserSortPoints().size(), userTestDataProperties.getUserSortTrophies().size());
        int length = userTestDataProperties.getUserSortPoints().size();

        // Create Users
        List<UserModel> users = new ArrayList<>();

        for (int i = 0; i < length; ++i) {
            UserModel toAdd = new UserModel(testUser);
            toAdd.setType(UserType.REGULAR_USER);
            toAdd.setEmail(toAdd.getEmail() + i);
            toAdd.setTotalPoints(userTestDataProperties.getUserSortPoints().get(i));
            toAdd.setTrophies(userTestDataProperties.getUserSortTrophies().get(i));
            toAdd.setRafflesWon(userTestDataProperties.getUserRafflesWon());

            users.add(toAdd);
            usersToDelete.add(toAdd);

            dbFirestore.collection(COLLECTION_NAME).document(toAdd.getEmail()).set(toAdd.generateMap()).get();
        }

        // Sort the users
        users.sort(Collections.reverseOrder());

        // Receive the users from the database
        String response = mockMvc.perform(get("/user/sortByScore/" + length)
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        List<UserModel> receivedUsers = mapper.readValue(response, new TypeReference<>() {
        });

        assertEquals(users.size(), receivedUsers.size());

        for (int i = 0; i < length; ++i)
            assertEquals(users.get(i).getEmail(), receivedUsers.get(i).getEmail());
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(COLLECTION_NAME).document(testUser.getEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPostEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutInitialEmail()).delete().get();
        dbFirestore.collection(COLLECTION_NAME).document(userTestDataProperties.getUserPutChangedEmail()).delete().get();

        for (UserModel user : usersToDelete)
            dbFirestore.collection(COLLECTION_NAME).document(user.getEmail()).delete().get();
    }

    private UserModel getUserFromJSON(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode parentNode = mapper.readTree(json);

        String email = parentNode.path("email").asText(null);

        String password = parentNode.path("password").asText(null);

        String username = parentNode.path("username").asText(null);

        UserType userType = UserType.valueOf(parentNode.path("type").asText(null));

        String profilePicture = parentNode.path("profilePicture").asText(null);

        String accountStatus = parentNode.path("accountStatus").asText(null);

        if (userType == UserType.REGULAR_USER) {
            Long totalPoints = parentNode.path("totalPoints").asLong(0L);

            Long currentPoints = parentNode.path("currentPoints").asLong(0L);

            Long trophies = parentNode.path("trophies").asLong(0L);

            Boolean vipStatus = parentNode.path("vipStatus").asBoolean(false);

            Long rafflesWon = parentNode.path("rafflesWon").asLong(0L);

            return new UserModel(email, password, username, userType, profilePicture, accountStatus,
                    totalPoints, currentPoints, trophies, vipStatus, rafflesWon);
        } else {
            return new UserModel(email, password, username, userType, profilePicture, accountStatus);
        }
    }
}
