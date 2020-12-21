package com.techflow.techhubbackend.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.config.ThreadCategoriesProperties;
import com.techflow.techhubbackend.model.ThreadModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ThreadService {

    public static final String COLLECTION_NAME = "thread";

    @Autowired
    private Firestore dbFirestore;

    @Autowired
    private ThreadCategoriesProperties threadCategoriesProperties;

    public List<ThreadModel> getAllThreads() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

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
        }
        else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Thread not found");
        }
    }

    public String createThread(ThreadModel thread) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        thread.setId(documentReference.getId());
        documentReference.set(thread.generateMap()).get();

        return documentReference.getId();
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

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<String> getCategories() {
        return threadCategoriesProperties.getCategories();
    }

    public List<ThreadModel> getThreadsByCategory(String category) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("category", category).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ThreadModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .sorted(Comparator.comparing(ThreadModel::getDateCreated).reversed())
                .collect(Collectors.toList());
    }
}
