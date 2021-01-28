package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThreadModel {

    private String id;
    private String ownerEmail;
    private String title;
    private String category;
    private String text;
    private Date dateCreated;
    private Boolean hasTrophy;
    private Boolean vipStatus;
    private Boolean isLocked;
    private Boolean isReported;

    public ThreadModel() {
    }

    public ThreadModel(String id, String ownerEmail, String title, String category, String text, Date dateCreated, Boolean hasTrophy, Boolean vipStatus, Boolean isLocked, Boolean isReported) {
        this.id = id;
        this.ownerEmail = ownerEmail;
        this.title = title;
        this.category = category;
        this.text = text;
        this.dateCreated = dateCreated;
        this.hasTrophy = hasTrophy;
        this.vipStatus = vipStatus;
        this.isLocked = isLocked;
        this.isReported = isReported;
    }

    public ThreadModel(ThreadModel threadModel) {
        this.id = threadModel.id;
        this.ownerEmail = threadModel.ownerEmail;
        this.title = threadModel.title;
        this.category = threadModel.category;
        this.text = threadModel.text;
        this.dateCreated = threadModel.dateCreated;
        this.hasTrophy = threadModel.hasTrophy;
        this.vipStatus = threadModel.vipStatus;
        this.isLocked = threadModel.isLocked;
        this.isReported = threadModel.isReported;
    }

    public ThreadModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.ownerEmail = (String) map.getOrDefault("ownerEmail", "no owner");
        this.title = (String) map.getOrDefault("title", "no title");
        this.category = (String) map.getOrDefault("category", "No Category");
        this.text = (String) map.getOrDefault("text", "no text");
        this.hasTrophy = (Boolean) map.getOrDefault("hasTrophy", false);
        this.vipStatus = (Boolean) map.getOrDefault("vipStatus", false);
        this.isLocked = (Boolean) map.getOrDefault("isLocked", false);
        this.isReported = (Boolean) map.getOrDefault("isReported", false);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Boolean getHasTrophy() { return hasTrophy; }

    public void setHasTrophy(Boolean hasTrophy) { this.hasTrophy = hasTrophy; }

    public Boolean getVipStatus() { return vipStatus; }

    public void setVipStatus(Boolean vipStatus) { this.vipStatus = vipStatus; }

    public Boolean getIsLocked() { return isLocked; }

    private void setIsLocked(Boolean isLocked) { this.isLocked = isLocked; }

    public Boolean getIsReported() { return isReported; }

    public void setIsReported(Boolean reported) { isReported = reported; }

    public ThreadModel builderSetDateCreated(Timestamp dateCreated) {
        this.dateCreated = dateCreated.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "ThreadModel{" +
                "id='" + id + '\'' +
                ", ownerEmail='" + ownerEmail + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", dateCreated=" + dateCreated +
                ", hasTrophy=" + hasTrophy +
                ", vipStatus=" + vipStatus +
                ", isReported=" + isReported +
                ", isLocked= " + isLocked +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ownerEmail, title, category, text, dateCreated, hasTrophy, vipStatus, isReported, isLocked);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ThreadModel threadModel = (ThreadModel) obj;

        return id.equals(threadModel.id) &&
                ownerEmail.equals(threadModel.ownerEmail) &&
                title.equals(threadModel.title) &&
                category.equals(threadModel.category) &&
                text.equals(threadModel.text) &&
                dateCreated.equals(threadModel.dateCreated) &&
                hasTrophy.equals(threadModel.hasTrophy) &&
                isReported.equals(threadModel.isReported) &&
                isLocked.equals(threadModel.isLocked) &&
                vipStatus.equals(threadModel.vipStatus);
    }

    public Map<String, Object> generateMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("ownerEmail", ownerEmail);
        map.put("title", title);
        map.put("category", category);
        map.put("text", text);
        map.put("hasTrophy", hasTrophy);
        map.put("vipStatus", vipStatus);
        map.put("isReported", isReported);
        map.put("isLocked", isLocked);
        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (ownerEmail != null || includeEmptyFields)
            map.put("ownerEmail", ownerEmail);
        if (title != null || includeEmptyFields)
            map.put("title", title);
        if (category != null || includeEmptyFields)
            map.put("category", category);
        if (text != null || includeEmptyFields)
            map.put("text", text);
        if (hasTrophy != null || includeEmptyFields)
            map.put("hasTrophy", hasTrophy);
        if (vipStatus != null || includeEmptyFields)
            map.put("vipStatus", vipStatus);
        if (isReported != null || includeEmptyFields)
            map.put("isReported", isReported);
        if (isLocked != null || includeEmptyFields)
            map.put("isLocked", isLocked);

        return map;
    }
}
