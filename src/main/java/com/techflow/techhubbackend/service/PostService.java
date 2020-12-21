package com.techflow.techhubbackend.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.PostModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class PostService {

    public static final String COLLECTION_NAME = "post";

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

    public String createPost(PostModel postModel) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();
        postModel.setId(documentReference.getId());
        documentReference.set(postModel.generateMap()).get();

        return documentReference.getId();
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

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<PostModel> getPostsByThreadId(String threadId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("threadId", threadId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public long getPostsCountByThreadId(String threadId) throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("threadId", threadId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .count();
    }
}
