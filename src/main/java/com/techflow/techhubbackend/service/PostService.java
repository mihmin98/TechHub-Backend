package com.techflow.techhubbackend.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.PostModel;
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
public class PostService {

    public static final String COLLECTION_NAME = "post";
    public static final String THREAD_COLLECTION_NAME = "thread";

    @Autowired
    ThreadService threadService;

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
        documentReference.set(postModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("postId", documentReference.getId());

        if(postModel.isHasTrophy())
        {
            ThreadModel threadModel = new ThreadModel(threadService.getThread(postModel.getThreadId()));
            threadModel.setHasTrophy(true);
            threadService.updateThread(postModel.getThreadId(), threadModel);
        }

        return mapper.writeValueAsString(node);
    }

    public void updatePost(String id, PostModel postModel) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(postModel.generateMap(false)).get();

        PostModel existentPostModel = getPost(id);
        if(existentPostModel.isHasTrophy())
        {
            ThreadModel threadModel = new ThreadModel(threadService.getThread(existentPostModel.getThreadId()));
            threadModel.setHasTrophy(true);
            threadService.updateThread(existentPostModel.getThreadId(), threadModel);
        }
    }

    public void deletePost(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");

        PostModel postModel = new PostModel(Objects.requireNonNull(documentReference.getData()));
        Long postNumber = postModel.getPostNumber();

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereGreaterThan("postNumber", postNumber).get().get().getDocuments();
        List<PostModel> postsToUpdate = documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new PostModel(mapTimestampEntry.getKey()).builderSetDateCreated(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
        for (PostModel postToUpdate : postsToUpdate) {
            postToUpdate.setPostNumber(postToUpdate.getPostNumber() - 1);
            updatePost(postToUpdate.getId(), postToUpdate);
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
}
