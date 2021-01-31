package com.techflow.techhubbackend.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.techflow.techhubbackend.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.techflow.techhubbackend.security.SecurityConstants.AUTH_TOKEN_PREFIX;

@Service
public class ReportService {
    public static final String COLLECTION_NAME = "report";
    public static final String THREAD_COLLECTION_NAME = "thread";
    public static final String POST_COLLECTION_NAME = "post";

    @Autowired
    ThreadService threadService;

    @Autowired
    PostService postService;

    @Autowired
    private Firestore dbFirestore;

    public List<ReportModel> getAllReports() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ReportModel(mapTimestampEntry.getKey()).builderSetDateReported(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public ReportModel getReport(String id, String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MODERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only moderators can access this endpoint.");
        }

        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        ReportModel reportModel;
        if (documentSnapshot.exists()) {
            reportModel = new ReportModel(Objects.requireNonNull(documentSnapshot.getData()));
            reportModel.setDateReported(Objects.requireNonNull(documentSnapshot.getCreateTime()).toDate());
            return reportModel;
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");
        }
    }

    public String createReport(ReportModel reportModel, String jwt) throws ExecutionException, InterruptedException, JsonProcessingException {

        if (getUserTypeFromJwt(jwt) != UserType.REGULAR_USER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only regular users can access this endpoint.");
        }

        validateReportedItem(reportModel);

        DocumentReference documentReference = dbFirestore.collection(COLLECTION_NAME).document();

        reportModel.setId(documentReference.getId());
        reportModel.setReporterId(getEmailFromJwt(jwt));
        if (reportModel.getReportType() == null) {
            reportModel.setReportType(ReportType.OTHERS);
        }
        if (reportModel.getIsResolved() == null) {
            reportModel.setIsResolved(false);
        }

        if (reportModel.getIsPostReport()) {
            PostModel postModel = new PostModel();
            postModel.setIsReported(true);
            postService.updatePost(reportModel.getReportedItemId(), postModel);
        } else {
            ThreadModel threadModel = new ThreadModel();
            threadModel.setIsReported(true);
            threadService.updateThread(reportModel.getReportedItemId(), threadModel, true);
        }

        documentReference.set(reportModel.generateMap()).get();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        node.put("id", documentReference.getId());

        return mapper.writeValueAsString(node);
    }

    public void updateReport(String id, ReportModel reportModel, String jwt) throws ExecutionException, InterruptedException {

        DocumentSnapshot documentSnapshot = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentSnapshot.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).update(reportModel.generateMap(false)).get();

        ReportModel initialReportModel = getReport(id, jwt);
        List<ReportModel> allReportsForCurrentReportedItem = getReportsByReportedItemIdId(initialReportModel.getReportedItemId(), jwt);
        boolean allReportsHaveBeenSolved = true;
        for (ReportModel report : allReportsForCurrentReportedItem) {
            if (!report.getIsResolved()) {
                allReportsHaveBeenSolved = false;
                break;
            }
        }

        if (allReportsHaveBeenSolved) {
            if (initialReportModel.getIsPostReport()) {
                PostModel postModel = new PostModel();
                postModel.setIsReported(false);
                postService.updatePost(initialReportModel.getReportedItemId(), postModel);
            } else {
                ThreadModel threadModel = new ThreadModel();
                threadModel.setIsReported(false);
                threadService.updateThread(initialReportModel.getReportedItemId(), threadModel, true);
            }
        }
    }

    public void deleteReport(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot documentReference = dbFirestore.collection(COLLECTION_NAME).document(id).get().get();

        if (!documentReference.exists())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Report not found");

        dbFirestore.collection(COLLECTION_NAME).document(id).delete().get();
    }

    public List<String> getReportTypes() {
        List<String> reportTypes = new ArrayList<>();
        Object[] possibleValues = ReportType.values();
        for (Object value : possibleValues) {
            reportTypes.add(value.toString());
        }

        return reportTypes;
    }

    public List<ReportModel> getSortedReportsByImportance(String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MODERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only moderators can access this endpoint.");
        }

        List<ReportModel> sortedReportsByImportance = new ArrayList<>();

        List<String> sortedReportedItemsIds = getSortedReportedItemsIds();
        for (String reportedItemId : sortedReportedItemsIds) {
            sortedReportsByImportance.addAll(getReportsByReportedItemIdId(reportedItemId, jwt));
        }

        return sortedReportsByImportance;
    }

    public List<ReportModel> getReportsByReportedItemIdId(String reportedItemId, String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MODERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only moderators can access this endpoint.");
        }

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("reportedItemId", reportedItemId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ReportModel(mapTimestampEntry.getKey()).builderSetDateReported(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public List<ReportModel> getReportsByReportedItemIdIdUnauthorized(String reportedItemId) throws ExecutionException, InterruptedException {

        List<QueryDocumentSnapshot> documentSnapshots = dbFirestore.collection(COLLECTION_NAME).whereEqualTo("reportedItemId", reportedItemId).get().get().getDocuments();

        return documentSnapshots.stream()
                .map(queryDocumentSnapshot -> Map.entry(queryDocumentSnapshot.getData(), Objects.requireNonNull(queryDocumentSnapshot.getCreateTime())))
                .map(mapTimestampEntry -> new ReportModel(mapTimestampEntry.getKey()).builderSetDateReported(mapTimestampEntry.getValue()))
                .collect(Collectors.toList());
    }

    public List<PostModel> getReportedPosts(String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MODERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only moderators can access this endpoint.");
        }

        List<PostModel> reportedPosts = new ArrayList<>();

        List<String> reportedPostsIds = getReportedItemsIds();
        for (String reportedPostId : reportedPostsIds) {
            DocumentSnapshot documentSnapshot = dbFirestore.collection(POST_COLLECTION_NAME).document(reportedPostId).get().get();
            if (documentSnapshot.exists()) {
                PostModel postModel = postService.getPost(reportedPostId);
                reportedPosts.add(postModel);
            }
        }

        return reportedPosts;
    }

    public List<ThreadModel> getReportedThreads(String jwt) throws ExecutionException, InterruptedException {

        if (getUserTypeFromJwt(jwt) != UserType.MODERATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only moderators can access this endpoint.");
        }

        List<ThreadModel> reportedThreads = new ArrayList<>();

        List<String> reportedThreadsIds = getReportedItemsIds();
        for (String reportedThreadId : reportedThreadsIds) {
            DocumentSnapshot documentSnapshot = dbFirestore.collection(THREAD_COLLECTION_NAME).document(reportedThreadId).get().get();
            if (documentSnapshot.exists()) {
                ThreadModel threadModel = threadService.getThread(reportedThreadId, true);
                reportedThreads.add(threadModel);
            }
        }

        return reportedThreads;
    }

    private List<String> getReportedItemsIds() throws ExecutionException, InterruptedException {

        Set<String> reportedItemsIds = new HashSet<>();
        List<ReportModel> reportModels = getAllReports();
        for (ReportModel reportModel : reportModels) {
            reportedItemsIds.add(reportModel.getReportedItemId());
        }

        return new ArrayList<>(reportedItemsIds);
    }

    private List<String> getSortedReportedItemsIds() throws ExecutionException, InterruptedException {

        Map<String, Integer> reportedItemsMap = new HashMap<>();
        List<ReportModel> reportModels = getAllReports();

        for (ReportModel reportModel : reportModels) {
            String reportedItemId = reportModel.getReportedItemId();
            if (reportedItemsMap.containsKey(reportedItemId)) {
                reportedItemsMap.put(reportedItemId, reportedItemsMap.get(reportedItemId) + 1);
            } else {
                reportedItemsMap.put(reportedItemId, 1);
            }
        }

        Stream<Map.Entry<String, Integer>> sorted =
                reportedItemsMap.entrySet().stream()
                        .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()));

        return sorted.map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private void validateReportedItem(ReportModel reportModel) throws ExecutionException, InterruptedException {

        if (reportModel.getIsPostReport() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request. isPostReport should not be null.");
        }
        if (reportModel.getIsPostReport()) {
            DocumentSnapshot documentSnapshot = dbFirestore.collection(POST_COLLECTION_NAME).document(reportModel.getReportedItemId()).get().get();
            if (!documentSnapshot.exists()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request. Post id does not exist.");
            }
        } else {
            DocumentSnapshot documentSnapshot = dbFirestore.collection(THREAD_COLLECTION_NAME).document(reportModel.getReportedItemId()).get().get();
            if (!documentSnapshot.exists()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request. Thread id does not exist.");
            }
        }
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
