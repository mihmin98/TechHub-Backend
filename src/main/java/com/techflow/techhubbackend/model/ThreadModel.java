package com.techflow.techhubbackend.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThreadModel {

    private String ownerEmail;
    private String title;
    private String text;
    private Date dateCreated;

    public ThreadModel() {
    }

    public ThreadModel(String ownerEmail, String title, String text, Date dateCreated) {
        this.ownerEmail = ownerEmail;
        this.title = title;
        this.text = text;
        this.dateCreated = dateCreated;
    }

    public ThreadModel(ThreadModel threadModel) {
        this.ownerEmail = threadModel.ownerEmail;
        this.title = threadModel.title;
        this.text = threadModel.text;
        this.dateCreated = threadModel.dateCreated;
    }

    public ThreadModel(Map<String, Object> map) {
        this.ownerEmail = (String) map.getOrDefault("ownerEmail", "no owner");
        this.title = (String) map.getOrDefault("title", "no title");
        this.text = (String) map.getOrDefault("text", "no text");
        this.dateCreated = (Date) map.getOrDefault("dateCreated", null);
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

    @Override
    public String toString() {
        return "ThreadModel{" +
                "ownerEmail=\"" + ownerEmail + "\", " +
                "title=\"" + title + "\", " +
                "text=\"" + text + "\", " +
                "dateCreated=\"" + dateCreated + "\"}";
    }

    @Override
    public int hashCode() {
        return Objects.hash(ownerEmail, title, text, dateCreated);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;

        ThreadModel threadModel = (ThreadModel) obj;

        return ownerEmail.equals(threadModel.ownerEmail) &&
                title.equals(threadModel.title) &&
                text.equals(threadModel.text) &&
                dateCreated.equals(threadModel.dateCreated);
    }

    public Map<String, Object> getMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("ownerEmail", ownerEmail);
        map.put("title", title);
        map.put("text", text);
        map.put("dateCreated", dateCreated);

        return map;
    }

    public Map<String, Object> getMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (ownerEmail != null || includeEmptyFields)
            map.put("ownerEmail", ownerEmail);
        if (title != null || includeEmptyFields)
            map.put("title", title);
        if (text != null || includeEmptyFields)
            map.put("text", text);
        if (dateCreated != null || includeEmptyFields)
            map.put("dateCreated", dateCreated);

        return map;
    }
}
