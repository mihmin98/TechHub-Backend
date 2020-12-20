package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.*;

public class PostModel {

    private String userEmail;
    private  String threadId;
    private int postNumber;
    private String text;
    private Date dateCreated;
    private Boolean hasTrophy;
    private Set<String> upvotes; //unique user emails
    private Set<String> downvotes;

    public PostModel() {
    }

    public PostModel(String userEmail, String threadId, int postNumber, String text, Date dateCreated, boolean hasTrophy, Set<String> upvotes, Set<String> downvotes) {
        this.userEmail = userEmail;
        this.threadId = threadId;
        this.postNumber = postNumber;
        this.text = text;
        this.dateCreated = dateCreated;
        this.hasTrophy = hasTrophy;
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    public PostModel(PostModel postModel)
    {
        this.userEmail=postModel.getUserEmail();
        this.threadId=postModel.getThreadId();
        this.postNumber=postModel.getPostNumber();
        this.text=postModel.getText();
        this.dateCreated=postModel.getDateCreated();
        this.hasTrophy=postModel.isHasTrophy();
        this.upvotes=postModel.getUpvotes();
        this.downvotes=postModel.getDownvotes();
    }

    public PostModel(Map<String, Object> map) {
        this.userEmail = (String) map.getOrDefault("userEmail", "no email");
        this.threadId=(String) map.getOrDefault("threadId","no thread id");
        this.postNumber=(Integer) map.getOrDefault("postNumber", 0);
        this.text=(String) map.getOrDefault("text", "no text");
        //this.dateCreated=(Date) map.getOrDefault("dateCreated","2018-05-11T17:24:42.980");
        this.hasTrophy=(Boolean) map.getOrDefault("hasTrophy", null);
        this.upvotes=new HashSet<String>((List<String>) map.getOrDefault("upvotes", null));
        this.downvotes=new HashSet<String>((List<String>) map.getOrDefault("downvotes", null));
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getThreadId() {
        return threadId;
    }

    public int getPostNumber() {
        return postNumber;
    }

    public String getText() {
        return text;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Boolean isHasTrophy() {
        return hasTrophy;
    }

    public Set<String> getUpvotes() {
        return upvotes;
    }

    public Set<String> getDownvotes() {
        return downvotes;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public void setPostNumber(int postNumber) {
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

    public PostModel builderSetDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "PostModel{" +
                "userEmail='" + userEmail + '\'' +
                ", threadId='" + threadId + '\'' +
                ", postNumber=" + postNumber +
                ", text='" + text + '\'' +
                ", dateCreated=" + dateCreated +
                ", hasTrophy=" + hasTrophy +
                ", upvotes=" + upvotes +
                ", downvotes=" + downvotes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PostModel postModel = (PostModel) o;
        return postNumber == postModel.postNumber &&
                hasTrophy == postModel.hasTrophy &&
                Objects.equals(userEmail, postModel.userEmail) &&
                Objects.equals(threadId, postModel.threadId) &&
                Objects.equals(text, postModel.text) &&
                Objects.equals(dateCreated, postModel.dateCreated) &&
                Objects.equals(upvotes, postModel.upvotes) &&
                Objects.equals(downvotes, postModel.downvotes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userEmail, threadId, postNumber, text, dateCreated, hasTrophy, upvotes, downvotes);
    }

    public Map<String, Object> generateMap() {
        Map<java.lang.String, java.lang.Object> map = new HashMap<>();

        map.put("userEmail", userEmail);
        map.put("threadId", threadId);
        map.put("postNumber", postNumber);
        map.put("text", text);
        map.put("dateCreated", dateCreated);
        map.put("hasTrophy", hasTrophy);
        map.put("upvotes", Arrays.asList(upvotes.toArray()));
        map.put("downvotes", Arrays.asList(downvotes.toArray()));

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (userEmail != null || includeEmptyFields)
            map.put("userEmail", userEmail);
        if (threadId != null || includeEmptyFields)
            map.put("threadId", threadId);
        if (postNumber != 0 || includeEmptyFields)
            map.put("postNumber", postNumber);
        if (text != null || includeEmptyFields)
            map.put("text", text);
        if (dateCreated != null || includeEmptyFields)
            map.put("dateCreated", dateCreated);
        if (hasTrophy != null || includeEmptyFields)
            map.put("hasTrophy", hasTrophy);
        if (upvotes.isEmpty() || includeEmptyFields)
            map.put("upvotes", Arrays.asList(upvotes.toArray()));
        if (downvotes.isEmpty() || includeEmptyFields)
            map.put("downvotes", Arrays.asList(downvotes.toArray()));

        return map;
    }
}
