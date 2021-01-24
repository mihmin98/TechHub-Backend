package com.techflow.techhubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.config.ThreadCategoriesProperties;
import com.techflow.techhubbackend.model.PostModel;
import com.techflow.techhubbackend.model.ThreadModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ThreadService {

    public static final String COLLECTION_NAME = "thread";

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    private ThreadCategoriesProperties threadCategoriesProperties;

    public List<ThreadModel> getThreadsByTitle(String title, Boolean vipStatus) throws ExecutionException, InterruptedException {

        title = title.toLowerCase();
        List<ThreadModel> convertedList = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("vipStatus", vipStatus).get().get().getDocuments().stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ThreadModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());

        List<ThreadModel> returnList = new ArrayList<>();

        for (var thread : convertedList) {
            if (thread.getTitle() == null)
                 continue;
            Pattern pattern = Pattern.compile(title);
            Matcher matcherTitle = pattern.matcher(thread.getTitle().toLowerCase());
            Matcher matcherText = pattern.matcher(thread.getText().toLowerCase());
            if (matcherTitle.find() || matcherText.find()) {
                returnList.add(thread);
            }
        }
        return returnList;
    }

    public List<ThreadModel> getAllThreads(Boolean vipStatus) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("vipStatus", vipStatus).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ThreadModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public ThreadModel getThread(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        ThreadModel threadModel;
        if (documentReference.exists()) {
            threadModel = new ThreadModel(Objects.requireNonNull(documentReference.getData()));
            threadModel.setDateCreated(Objects.requireNonNull(documentReference.getCreateTime()).toDate());
            return threadModel;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");
        }
    }

    public String createThread(ThreadModel thread) throws ExecutionException, InterruptedException, JsonProcessingException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        thread.setHasTrophy(false);
        documentReference.set(thread.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("threadId", documentReference.getId());

        return mapper.writeValueAsString(node);

    }

    public void updateThread(String id, ThreadModel thread) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(thread.generateMap(false)).get();
    }

    public void deleteThread(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection("post").whereEqualTo("threadId", id).get().get().getDocuments();

        documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .forEach(postToDelete -> {
                    try {
                        dbFirestore.collection("post").document(postToDelete.getId()).delete().get();
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<String> getCategories() {
        return threadCategoriesProperties.getCategories();
    }

    public List<ThreadModel> getThreadsByCategory(String category, Boolean vipStatus) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("vipStatus", vipStatus).whereEqualTo("category", category).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ThreadModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .sorted(Comparator.comparing(ThreadModel::getDateCreated).reversed())
                .collect(Collectors.toList());
    }
}
