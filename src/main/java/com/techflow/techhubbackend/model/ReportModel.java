package com.techflow.techhubbackend.model;

import com.google.cloud.Timestamp;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportModel {
    private String id;
    private String reporterId;
    private String reportedItemId;
    private ReportType reportType;
    private String description;
    private Date dateReported;
    private Boolean isResolved;
    private Boolean isPostReport; //a report can refer to a post or a thread

    public ReportModel() {
    }

    public ReportModel(String id, String reporterId, String reportedItemId, ReportType reportType, String description, Date dateReported, Boolean isResolved, Boolean isPostReport) {
        this.id = id;
        this.reporterId = reporterId;
        this.reportedItemId = reportedItemId;
        this.reportType = reportType;
        this.description = description;
        this.dateReported = dateReported;
        this.isResolved = isResolved;
        this.isPostReport = isPostReport;
    }

    public ReportModel(ReportModel reportModel) {
        this.id = reportModel.id;
        this.reporterId = reportModel.reporterId;
        this.reportedItemId = reportModel.reportedItemId;
        this.reportType = reportModel.reportType;
        this.description = reportModel.description;
        this.dateReported = reportModel.dateReported;
        this.isResolved = reportModel.isResolved;
        this.isPostReport = reportModel.isPostReport;
    }

    public ReportModel(Map<String, Object> map) {
        this.id = (String) map.getOrDefault("id", "no id");
        this.reporterId = (String) map.getOrDefault("reporterId", "no reporter id");
        this.reportedItemId = (String) map.getOrDefault("reportedItemId", "no reported item id");
        this.reportType = ReportType.valueOf(map.getOrDefault("reportType", ReportType.OTHERS).toString());
        this.description = (String) map.getOrDefault("description", "no description");
        this.isResolved = (Boolean) map.getOrDefault("isResolved", false);
        this.isPostReport = (Boolean) map.getOrDefault("isPostReport", null);
    }

    public String getId() {
        return id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public String getReportedItemId() {
        return reportedItemId;
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

    public Boolean getIsResolved() { return isResolved; }

    public Boolean getIsPostReport() { return isPostReport; }

    public void setId(String id) {
        this.id = id;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public void setReportedItemId(String reportedItemId) {
        this.reportedItemId = reportedItemId;
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

    public void setIsResolved(Boolean resolved) {
        this.isResolved = resolved;
    }

    public void setIsPostReport(Boolean postReport) { isPostReport = postReport; }

    public ReportModel builderSetDateReported(Timestamp dateReported) {
        this.dateReported = dateReported.toDate();
        return this;
    }

    @Override
    public String toString() {
        return "ReportModel{" +
                "id='" + id + '\'' +
                ", reporterId='" + reporterId + '\'' +
                ", reportedItemId='" + reportedItemId + '\'' +
                ", reportType=" + reportType +
                ", description='" + description + '\'' +
                ", dateReported=" + dateReported +
                ", isResolved=" + isResolved +
                ", isPostReport=" + isPostReport +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportModel that = (ReportModel) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(reporterId, that.reporterId) &&
                Objects.equals(reportedItemId, that.reportedItemId) &&
                reportType == that.reportType &&
                Objects.equals(description, that.description) &&
                Objects.equals(dateReported, that.dateReported) &&
                Objects.equals(isResolved, that.isResolved) &&
                Objects.equals(isPostReport, that.isPostReport);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reporterId, reportedItemId, reportType, description, dateReported, isResolved, isPostReport);
    }

    public Map<String, Object> generateMap() {
        Map<String, Object> map = new HashMap<>();

        map.put("id", id);
        map.put("reportedId", reporterId);
        map.put("reportedItemId", reportedItemId);
        map.put("reportType", reportType);
        map.put("description", description);
        map.put("isResolved", isResolved);
        map.put("isPostReport", isPostReport);

        return map;
    }

    public Map<String, Object> generateMap(boolean includeEmptyFields) {
        Map<String, Object> map = new HashMap<>();

        if (id != null || includeEmptyFields)
            map.put("id", id);
        if (reporterId != null || includeEmptyFields)
            map.put("reportedId", reporterId);
        if (reportedItemId != null || includeEmptyFields)
            map.put("reportedItemId", reportedItemId);
        if (reportType != null || includeEmptyFields)
            map.put("reportType", reportType);
        if (description != null || includeEmptyFields)
            map.put("description", description);
        if (isResolved != null || includeEmptyFields)
            map.put("isResolved", isResolved);
        if (isPostReport != null || includeEmptyFields)
            map.put("isPostReport", isPostReport);

        return map;
    }
}
