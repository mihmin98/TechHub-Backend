package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportModel {
    private String id;
    private String reporterId;
    private String postId;
    private ReportType reportType;
    private String description;
    private Date dateReported;
    private Boolean isResolved;

    public ReportModel() {
    }

    public ReportModel(String id, String reporterId, String postId, ReportType reportType, String description, Date dateReported, Boolean isResolved) {
        this.id = id;
        this.reporterId = reporterId;
        this.postId = postId;
        this.reportType = reportType;
        this.description = description;
        this.dateReported = dateReported;
        this.isResolved = isResolved;
    }

    public ReportModel(ReportModel reportModel) {
        this.id = reportModel.id;
        this.reporterId = reportModel.reporterId;
        this.postId = reportModel.postId;
        this.reportType = reportModel.reportType;
        this.description = reportModel.description;
        this.dateReported = reportModel.dateReported;
        this.isResolved = reportModel.isResolved;
    }

    public ReportModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.reporterId = (String) map.getOrDefault("reporterId", "no reporter id");
        this.postId = (String) map.getOrDefault("postId", "no post id");
        this.reportType = ReportType.valueOf(map.getOrDefault("reportType", ReportType.OTHERS).toString());
        this.description = (String) map.getOrDefault("description", "no description");
        this.isResolved = (Boolean) map.getOrDefault("isResolved", false);
    }

    public String getId() {
        return id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getPostId() {
        return postId;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public String getDescription() {
        return description;
    }

    public Date getDateReported() {
        return dateReported;
    }

    public Boolean getResolved() {
        return isResolved;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateReported(Date dateReported) {
        this.dateReported = dateReported;
    }

    public void setResolved(Boolean resolved) {
        isResolved = resolved;
    }

    public ReportModel builderSetDateReported(Timestamp dateReported) {
        this.dateReported = dateReported.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "ReportModel{" +
                "id='" + id + '\'' +
                ", reporterId='" + reporterId + '\'' +
                ", postId='" + postId + '\'' +
                ", reportType=" + reportType +
                ", description='" + description + '\'' +
                ", dateReported=" + dateReported +
                ", isResolved=" + isResolved +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportModel that = (ReportModel) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(reporterId, that.reporterId) &&
                Objects.equals(postId, that.postId) &&
                reportType == that.reportType &&
                Objects.equals(description, that.description) &&
                Objects.equals(dateReported, that.dateReported) &&
                Objects.equals(isResolved, that.isResolved);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reporterId, postId, reportType, description, dateReported, isResolved);
    }

    public Map<String, Object> generateMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("reportedId", reporterId);
        map.put("postId", postId);
        map.put("reportType", reportType);
        map.put("description", description);
        map.put("isResolved", isResolved);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (reporterId != null || includeEmptyFields)
            map.put("reportedId", reporterId);
        if (postId != null || includeEmptyFields)
            map.put("postId", postId);
        if (reportType != null || includeEmptyFields)
            map.put("reportType", reportType);
        if (description != null || includeEmptyFields)
            map.put("description", description);
        if (isResolved != null || includeEmptyFields)
            map.put("isResolved", isResolved);

        return map;
    }
}
