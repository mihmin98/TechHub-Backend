package com.techflow.techhubbackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.techflow.techhubbackend.config.RaffleProperties;
import com.techflow.techhubbackend.model.RaffleModel;
import com.techflow.techhubbackend.model.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@Service
public class RaffleService {

    public static final String COLLECTION_NAME = "raffle";

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    private RaffleProperties raffleProperties;

    @Autowired
    private TaskScheduler taskScheduler;

    @PostConstruct
    public void init() throws ExecutionException, InterruptedException {
        RaffleModel activeRaffle = getActiveRaffle();

        if (activeRaffle == null)
            activeRaffle = createNewRaffle();

        taskScheduler.schedule(this::drawWinner, activeRaffle.getDrawTime().toDate());
    }

    public RaffleModel getActiveRaffle() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("isActive", true)
                .orderBy("createTime", Query.Direction.DESCENDING)
                .limit(1)
                .get().get();

        List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

        if (queryDocumentSnapshots.size() > 0)
            return new RaffleModel(queryDocumentSnapshots.get(0).getData());
        else
            return null;
    }

    private DocumentReference getActiveRaffleDocumentReference() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("isActive", true)
                .orderBy("createTime", Query.Direction.DESCENDING)
                .limit(1)
                .get().get();

        List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

        if (queryDocumentSnapshots.size() > 0)
            return queryDocumentSnapshots.get(0).getReference();
        else
            return null;
    }

    public RaffleModel getPreviousRaffle() throws ExecutionException, InterruptedException {
        QuerySnapshot querySnapshot = dbFirestore.collection(COLLECTION_NAME)
                .whereEqualTo("isActive", false)
                .orderBy("createTime", Query.Direction.DESCENDING)
                .limit(1)
                .get().get();

        List<QueryDocumentSnapshot> queryDocumentSnapshots = querySnapshot.getDocuments();

        if (queryDocumentSnapshots.size() > 0)
            return new RaffleModel(queryDocumentSnapshots.get(0).getData());
        else
            return null;
    }

    public RaffleModel getRaffleById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentSnapshot.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Raffle not found");

        return new RaffleModel(Objects.requireNonNull(documentSnapshot.getData()));
    }

    public List<RaffleModel> getAllRaffles() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> new RaffleModel(queryDocumentSnapshot.getData()))
                .collect(Collectors.toList());
    }

    public void registerUser(String jwt) throws ExecutionException, InterruptedException {
        if (getUserTypeFromJwt(jwt) != UserType.REGULAR_USER)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only regular users can register");

        DocumentReference raffleDocumentReference = getActiveRaffleDocumentReference();
        DocumentSnapshot raffleDocumentSnapshot = Objects.requireNonNull(raffleDocumentReference).get().get();
        RaffleModel activeRaffle = new RaffleModel(Objects.requireNonNull(raffleDocumentSnapshot.getData()));
        String userEmail = getEmailFromJwt(jwt);

        if (activeRaffle.getEntries().contains(userEmail))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already registered");

        DocumentReference userDocumentReference = dbFirestore.collection("user").document(userEmail);
        DocumentSnapshot userDocumentSnapshot = userDocumentReference.get().get();

        if (Objects.requireNonNull(userDocumentSnapshot.getLong("currentPoints")) < raffleProperties.getEntryCost())
            throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, "User does not have enough points");

        // Add user to entries and update prize
        activeRaffle.getEntries().add(userEmail);
        long updatedPrize = Objects.requireNonNull(raffleDocumentSnapshot.getLong("prize")) + ((Double) (raffleProperties.getEntryCost() * raffleProperties.getWinningsPercentage())).longValue();

        raffleDocumentReference.update("entries", activeRaffle.getEntries(),
                "prize", updatedPrize).get();

        // Remove cost from user points
        long updatedUserCurrentPoints = Objects.requireNonNull(userDocumentSnapshot.getLong("currentPoints")) - raffleProperties.getEntryCost();
        userDocumentReference.update("currentPoints", updatedUserCurrentPoints).get();
    }

    private void drawWinner() {
        try {
            RaffleModel raffle = getActiveRaffle();

            List<String> entries = raffle.getEntries();
            String winner = entries.get(new Random().nextInt(entries.size()));

            dbFirestore.collection(COLLECTION_NAME).document(raffle.getId()).update("winner", winner,
                    "isActive", false).get();

            // Update winner points
            DocumentReference userDocumentReference = dbFirestore.collection("user").document(winner);
            DocumentSnapshot userDocumentSnapshot = userDocumentReference.get().get();
            long updatedCurrentPoints = raffle.getPrize() + Objects.requireNonNull(userDocumentSnapshot.getLong("currentPoints"));
            long updatedTotalPoints = raffle.getPrize() + Objects.requireNonNull(userDocumentSnapshot.getLong("totalPoints"));

            userDocumentReference.update("currentPoints", updatedCurrentPoints,
                    "totalPoints", updatedTotalPoints).get();

            // Create and schedule new raffle
            RaffleModel newRaffle = createNewRaffle();
            taskScheduler.schedule(this::drawWinner, newRaffle.getDrawTime().toDate());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private RaffleModel createNewRaffle() throws ExecutionException, InterruptedException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();

        RaffleModel newRaffle = new RaffleModel(documentReference.getId(), 0L, new ArrayList<>(), null, null, null, true);

        documentReference.set(newRaffle.generateMap()).get();

        Timestamp createTime = documentReference.get().get().getCreateTime();

        // Create draw time
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Objects.requireNonNull(createTime).toSqlTimestamp());
        calendar.add(Calendar.DAY_OF_WEEK, raffleProperties.getDaysBetweenRaffles());

        Timestamp drawTime = Timestamp.of(calendar.getTime());

        documentReference.update("createTime", createTime,
                "drawTime", drawTime).get();

        newRaffle.setCreateTime(createTime);
        newRaffle.setDrawTime(drawTime);

        return newRaffle;
    }

    private UserType getUserTypeFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return UserType.valueOf(decodedJWT.getClaim("userType").asString());
    }

    private String getEmailFromJwt(String jwt) {
        DecodedJWT decodedJWT = JWT.decode(jwt.replace(AUTH_TOKEN_PREFIX, ""));
        return decodedJWT.getSubject();
    }
}
