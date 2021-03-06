package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.*;

public class PostModel {

    private String id;
    private String userEmail;
    private String threadId;
    private Long postNumber;
    private String text;
    private Date dateCreated;
    private Boolean hasTrophy;
    private Set<String> upvotes; //unique user emails
    private Set<String> downvotes;
    private Boolean isReported;

    public PostModel() {
    }

    public PostModel(String id, String userEmail, String threadId, Long postNumber, String text, Date dateCreated, boolean hasTrophy, Set<String> upvotes, Set<String> downvotes,  Boolean isReported) {
        this.id = id;
        this.userEmail = userEmail;
        this.threadId = threadId;
        this.postNumber = postNumber;
        this.text = text;
        this.dateCreated = dateCreated;
        this.hasTrophy = hasTrophy;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.isReported = isReported;
    }

    public PostModel(PostModel postModel) {
        this.id = postModel.getId();
        this.userEmail = postModel.getUserEmail();
        this.threadId = postModel.getThreadId();
        this.postNumber = postModel.getPostNumber();
        this.text = postModel.getText();
        this.dateCreated = postModel.getDateCreated();
        this.hasTrophy = postModel.isHasTrophy();
        this.upvotes =  new HashSet<>(postModel.getUpvotes());
        this.downvotes = new HashSet<>(postModel.getDownvotes());
        this.isReported = postModel.getIsReported();
    }

    public PostModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.userEmail = (String) map.getOrDefault("userEmail", "no email");
        this.threadId = (String) map.getOrDefault("threadId", "no thread id");
        this.postNumber = (Long) map.getOrDefault("postNumber", 0);
        this.text = (String) map.getOrDefault("text", "no text");
        this.hasTrophy = (Boolean) map.getOrDefault("hasTrophy", null);
        this.upvotes = new HashSet<String>((List<String>) map.getOrDefault("upvotes", new ArrayList<String>()));
        this.downvotes = new HashSet<String>((List<String>) map.getOrDefault("downvotes", new ArrayList<String>()));
        this.isReported = (Boolean) map.getOrDefault("isReported", false);
    }

    public String getId() { return id; }

    public String getUserEmail() {
        return userEmail;
    }

    public String getThreadId() { return threadId; }

    public Long getPostNumber() { return postNumber; }

    public String getText() {
        return text;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Boolean isHasTrophy() {
        if (hasTrophy == null) { return false; }
        return hasTrophy;
    }

    public Set<String> getUpvotes() {
        return upvotes;
    }

    public Set<String> getDownvotes() {
        return downvotes;
    }

    public Boolean getIsReported() { return isReported; }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setPostNumber(Long postNumber) {
        this.postNumber = postNumber;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public void setHasTrophy(Boolean hasTrophy) {
        this.hasTrophy = hasTrophy;
    }

    public void setUpvotes(Set<String> upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(Set<String> downvotes) {
        this.downvotes = downvotes;
    }

    public void setIsReported(Boolean reported) { isReported = reported; }

    public PostModel builderSetDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "PostModel{" +
                "id='" + id + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", threadId='" + threadId + '\'' +
                ", postNumber=" + postNumber +
                ", text='" + text + '\'' +
                ", dateCreated=" + dateCreated +
                ", hasTrophy=" + hasTrophy +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                ", isReported=" + isReported +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostModel postModel = (PostModel) o;
        return id.equals(postModel.id) &&
                postNumber.equals(postModel.postNumber) &&
                hasTrophy == postModel.hasTrophy &&
                Objects.equals(userEmail, postModel.userEmail) &&
                Objects.equals(threadId, postModel.threadId) &&
                Objects.equals(text, postModel.text) &&
                Objects.equals(dateCreated, postModel.dateCreated) &&
                Objects.equals(upvotes, postModel.upvotes) &&
                Objects.equals(downvotes, postModel.downvotes) &&
                Objects.equals(isReported, postModel.isReported);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userEmail, threadId, postNumber, text, dateCreated, hasTrophy, upvotes, downvotes, isReported);
    }

    public Map<String, Object> generateMap() {
        Map<java.lang.String, java.lang.Object> map = new HashMap<>();

        map.put("id", id);
        map.put("userEmail", userEmail);
        map.put("threadId", threadId);
        map.put("postNumber", postNumber);
        map.put("text", text);
        map.put("dateCreated", dateCreated);
        map.put("hasTrophy", hasTrophy);
        if(upvotes != null) { map.put("upvotes", Arrays.asList(upvotes.toArray())); }
        if(downvotes != null){ map.put("downvotes", Arrays.asList(downvotes.toArray())); }
        map.put("isReported", isReported);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (userEmail != null || includeEmptyFields)
            map.put("userEmail", userEmail);
        if (threadId != null || includeEmptyFields)
            map.put("threadId", threadId);
        if (postNumber != null || includeEmptyFields)
            map.put("postNumber", postNumber);
        if (text != null || includeEmptyFields)
            map.put("text", text);
        if (dateCreated != null || includeEmptyFields)
            map.put("dateCreated", dateCreated);
        if (hasTrophy != null || includeEmptyFields)
            map.put("hasTrophy", hasTrophy);
        if (upvotes != null || includeEmptyFields)
            map.put("upvotes", Arrays.asList(upvotes.toArray()));
        if (downvotes != null || includeEmptyFields)
            map.put("downvotes", Arrays.asList(downvotes.toArray()));
        if (isReported != null || includeEmptyFields)
            map.put("isReported", isReported);

        return map;
    }
}
