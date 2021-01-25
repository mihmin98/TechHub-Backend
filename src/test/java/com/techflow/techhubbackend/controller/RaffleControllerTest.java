package com.techflow.techhubbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.techflow.techhubbackend.config.RaffleControllerTestDataProperties;
import com.techflow.techhubbackend.config.RaffleProperties;
import com.techflow.techhubbackend.config.UserControllerTestDataProperties;
import com.techflow.techhubbackend.model.RaffleModel;
import com.techflow.techhubbackend.model.UserModel;
import com.techflow.techhubbackend.model.UserType;
import com.techflow.techhubbackend.security.SecurityConstants;
import com.techflow.techhubbackend.service.RaffleService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
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
public class RaffleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RaffleControllerTestDataProperties raffleTestDataProperties;

    @Autowired
    private UserControllerTestDataProperties userTestDataProperties;

    @Autowired
    private RaffleProperties raffleProperties;

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private RaffleService raffleService;

    private String jwt = null;

    private RaffleModel testRaffleModel;

    private final List<String> rafflesToDelete = new ArrayList<>();

    private final String RAFFLE_COLLECTION_NAME = "raffle";
    private final String USER_COLLECTION_NAME = "user";

    @BeforeAll
    void login() throws Exception {
        testRaffleModel = new RaffleModel(null, raffleTestDataProperties.getRafflePrize(), new ArrayList<>(), null, null, null, null);
        UserModel user = new UserModel(userTestDataProperties.getUserEmail(), userTestDataProperties.getUserPassword(), userTestDataProperties.getUserUsername(), userTestDataProperties.getUserType(), userTestDataProperties.getUserProfilePicture(), userTestDataProperties.getUserAccountStatus());
        user.setType(UserType.REGULAR_USER);
        user.setCurrentPoints(userTestDataProperties.getUserCurrentPoints());
        user.setTotalPoints(userTestDataProperties.getUserTotalPoints());
        user.setTrophies(userTestDataProperties.getUserTrophies());

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
    void getActiveRaffle() throws Exception {
        // Create a new active raffle
        DocumentReference documentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(documentReference.getId());
        raffle.setActive(true);

        documentReference.set(raffle.generateMap()).get();

        raffle.setCreateTime(documentReference.get().get().getCreateTime());

        documentReference.update("createTime", raffle.getCreateTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Get active raffle
        String raffleJson = mockMvc.perform(get("/raffle/active")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RaffleModel receivedRaffle = getRaffleFromJSON(raffleJson);

        // Check the received raffle
        assertEquals(raffle.getId(), receivedRaffle.getId());
        assertEquals(raffle.getPrize(), receivedRaffle.getPrize());
        assertEquals(raffle.getEntries(), receivedRaffle.getEntries());
        assertEquals(raffle.getCreateTime(), receivedRaffle.getCreateTime());
        assertEquals(raffle.getDrawTime(), receivedRaffle.getDrawTime());
        assertEquals(raffle.getWinner(), receivedRaffle.getWinner());
        assertEquals(true, raffle.getActive());
    }

    @Test
    void getPreviousRaffle() throws Exception {
        // Create a new non active raffle
        DocumentReference documentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(documentReference.getId());
        raffle.setActive(false);

        documentReference.set(raffle.generateMap()).get();

        raffle.setCreateTime(documentReference.get().get().getCreateTime());

        documentReference.update("createTime", raffle.getCreateTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Get previous raffle
        String raffleJson = mockMvc.perform(get("/raffle/previous")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RaffleModel receivedRaffle = getRaffleFromJSON(raffleJson);

        // Check the received raffle
        assertEquals(raffle.getId(), receivedRaffle.getId());
        assertEquals(raffle.getPrize(), receivedRaffle.getPrize());
        assertEquals(raffle.getEntries(), receivedRaffle.getEntries());
        assertEquals(raffle.getCreateTime(), receivedRaffle.getCreateTime());
        assertEquals(raffle.getDrawTime(), receivedRaffle.getDrawTime());
        assertEquals(raffle.getWinner(), receivedRaffle.getWinner());
        assertEquals(false, raffle.getActive());
    }

    @Test
    void getRaffle() throws Exception {
        // Get non existing raffle
        DocumentReference documentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        mockMvc.perform(get("/raffle/" + documentReference.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotFound());

        // Create a new raffle
        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(documentReference.getId());
        raffle.setActive(false);

        documentReference.set(raffle.generateMap()).get();

        raffle.setCreateTime(documentReference.get().get().getCreateTime());

        documentReference.update("createTime", raffle.getCreateTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Get raffle
        String raffleJson = mockMvc.perform(get("/raffle/" + raffle.getId())
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RaffleModel receivedRaffle = getRaffleFromJSON(raffleJson);

        // Check the received raffle
        assertEquals(raffle.getId(), receivedRaffle.getId());
        assertEquals(raffle.getPrize(), receivedRaffle.getPrize());
        assertEquals(raffle.getEntries(), receivedRaffle.getEntries());
        assertEquals(raffle.getCreateTime(), receivedRaffle.getCreateTime());
        assertEquals(raffle.getDrawTime(), receivedRaffle.getDrawTime());
        assertEquals(raffle.getWinner(), receivedRaffle.getWinner());
        assertEquals(raffle.getActive(), raffle.getActive());
    }

    @Test
    void getAllRaffles() throws Exception {
        // Create a new raffle
        DocumentReference documentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(documentReference.getId());
        raffle.setActive(false);

        documentReference.set(raffle.generateMap()).get();

        raffle.setCreateTime(documentReference.get().get().getCreateTime());

        documentReference.update("createTime", raffle.getCreateTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Get all raffles
        String rafflesJson = mockMvc.perform(get("/raffle")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        List<RaffleModel> receivedRaffles = getListOfRafflesFromJSON(rafflesJson);

        // Check if the created raffle is in the list
        boolean foundRaffle = false;

        for (RaffleModel r : receivedRaffles) {
            if (r.getId().equals(documentReference.getId())) {
                foundRaffle = true;
                break;
            }
        }

        assertTrue(receivedRaffles.size() > 0);
        assertTrue(foundRaffle);
    }

    @Test
    void registerUser() throws Exception {
        // Create a new active raffle
        DocumentReference documentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(documentReference.getId());
        raffle.setActive(true);
        raffle.setPrize(0L);

        documentReference.set(raffle.generateMap()).get();

        raffle.setCreateTime(documentReference.get().get().getCreateTime());

        documentReference.update("createTime", raffle.getCreateTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Give user 0 current points
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).update("currentPoints", 0).get();

        // Try to register
        mockMvc.perform(post("/raffle/register")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isNotAcceptable());

        // Give user enough points to register
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).update("currentPoints", raffleProperties.getEntryCost()).get();

        // Register
        mockMvc.perform(post("/raffle/register")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk());

        // Check that the prize has been updated and user points deducted
        long updatedPrize = Objects.requireNonNull(documentReference.get().get().getLong("prize"));
        assertEquals(((Double) (raffleProperties.getEntryCost() * raffleProperties.getWinningsPercentage())).longValue(), updatedPrize);

        long updatedUserCurrentPoints = Objects.requireNonNull(dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).get().get().getLong("currentPoints"));
        assertEquals(0, updatedUserCurrentPoints);

        // Try to register again
        mockMvc.perform(post("/raffle/register")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isConflict());
    }

    @Test
    void testDraw() throws Exception {
        // Get current user
        DocumentReference currentUserDocumentReference = dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail());
        DocumentSnapshot currentUserDocumentSnapshot = currentUserDocumentReference.get().get();

        long initialUserCurrentPoints = Objects.requireNonNull(currentUserDocumentSnapshot.getLong("currentPoints"));

        // Create a new active raffle
        DocumentReference raffleDocumentReference = dbFirestore.collection(RAFFLE_COLLECTION_NAME).document();

        RaffleModel raffle = new RaffleModel(testRaffleModel);
        raffle.setId(raffleDocumentReference.getId());
        raffle.setActive(true);
        raffle.getEntries().add(userTestDataProperties.getUserEmail());

        raffleDocumentReference.set(raffle.generateMap()).get();

        // Set the draw time to be 10s? from now
        raffle.setCreateTime(raffleDocumentReference.get().get().getCreateTime());
        Timestamp createTime = raffle.getCreateTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(createTime).toSqlTimestamp());
        calendar.add(Calendar.SECOND, raffleTestDataProperties.getRaffleDrawTimeDuration());

        Timestamp drawTime = Timestamp.of(calendar.getTime());
        raffle.setDrawTime(drawTime);

        raffleDocumentReference.update("createTime", raffle.getCreateTime(), "drawTime", raffle.getDrawTime()).get();

        rafflesToDelete.add(raffle.getId());

        // Schedule the created draw
        raffleService.init();

        // Wait for the draw
        Thread.sleep(raffleTestDataProperties.getRaffleSleepTime() * 1000);

        // Check that the draw has ended and a winner was chosen
        DocumentSnapshot raffleDocumentSnapshot = raffleDocumentReference.get().get();

        assertFalse(Objects.requireNonNull(raffleDocumentSnapshot.getBoolean("isActive")));
        assertNotNull(raffleDocumentSnapshot.getString("winner"));
        assertEquals(userTestDataProperties.getUserEmail(), raffleDocumentSnapshot.getString("winner"));

        // Check that the user's points have been updated
        currentUserDocumentSnapshot = currentUserDocumentReference.get().get();
        long updatedUserCurrentPoints = Objects.requireNonNull(currentUserDocumentSnapshot.getLong("currentPoints"));

        assertEquals(initialUserCurrentPoints + raffleTestDataProperties.getRafflePrize(), updatedUserCurrentPoints);

        // Check that a new draw was created (and add it to the delete list)
        String newRaffleJson = mockMvc.perform(get("/raffle/active")
                .header(SecurityConstants.AUTH_HEADER_STRING, jwt))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        RaffleModel newRaffle = getRaffleFromJSON(newRaffleJson);

        assertNotEquals(raffle.getId(), newRaffle.getId());

        rafflesToDelete.add(newRaffle.getId());
    }

    @AfterAll
    void cleanup() throws ExecutionException, InterruptedException {
        dbFirestore.collection(USER_COLLECTION_NAME).document(userTestDataProperties.getUserEmail()).delete().get();

        for (String id : rafflesToDelete)
            dbFirestore.collection(RAFFLE_COLLECTION_NAME).document(id).delete().get();
    }

    private RaffleModel getRaffleFromJSON(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode parentNode = mapper.readTree(json);

        return getRaffleFromJSON(parentNode);
    }

    private RaffleModel getRaffleFromJSON(JsonNode parentNode) {
        ObjectMapper mapper = new ObjectMapper();

        String id = parentNode.path("id").asText(null);

        long prize = parentNode.path("prize").asLong(0L);

        List<String> entries = new ArrayList<>();
        JsonNode entriesNode = parentNode.path("entries");
        Iterator<JsonNode> entriesIterator = entriesNode.elements();

        while (entriesIterator.hasNext())
            entries.add(entriesIterator.next().asText(null));

        Timestamp createTime = null;
        JsonNode createTimeNode = parentNode.path("createTime");
        if (createTimeNode != null && !createTimeNode.asText().equals("null")) {
            long seconds = createTimeNode.path("seconds").asLong();
            int nanos = createTimeNode.path("nanos").asInt();
            createTime = Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
        }

        Timestamp drawTime = null;
        JsonNode drawTimeNode = parentNode.path("drawTime");
        if (drawTimeNode != null && !drawTimeNode.asText().equals("null")) {
            long seconds = drawTimeNode.path("seconds").asLong();
            int nanos = drawTimeNode.path("nanos").asInt();
            drawTime = Timestamp.ofTimeSecondsAndNanos(seconds, nanos);
        }

        String winner = parentNode.path("winner").asText(null);

        Boolean isActive = parentNode.path("active").asBoolean();

        return new RaffleModel(id, prize, entries, createTime, drawTime, winner, isActive);
    }

    private List<RaffleModel> getListOfRafflesFromJSON(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode parentNode = mapper.readTree(json);

        List<RaffleModel> raffleList = new ArrayList<>();

        Iterator<JsonNode> raffleIterators = parentNode.elements();

        while (raffleIterators.hasNext())
            raffleList.add(getRaffleFromJSON(raffleIterators.next()));

        return raffleList;
    }
}
