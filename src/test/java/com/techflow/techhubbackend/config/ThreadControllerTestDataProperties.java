package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:ThreadControllerTest.properties")
public class ThreadControllerTestDataProperties {

    @Value("${thread.ownerEmail}")
    private String threadOwnerEmail;

    @Value("${thread.title}")
    private String threadTitle;

    @Value("${thread.category}")
    private String threadCategory;

    @Value("${thread.text}")
    private String threadText;

    @Value("${thread.hasTrophy}")
    private Boolean hasTrophy;

    @Value("${thread.vipStatus}")
    private Boolean vipStatus;

    @Value("${thread.isReported}")
    private Boolean isReported;

    @Value("${thread.put.text}")
    private String threadPutText;

    @Value("${thread.isLocked}")
    private Boolean isLocked;
    
    public ThreadControllerTestDataProperties() {
    }

    public String getThreadOwnerEmail() {
        return threadOwnerEmail;
    }

    public void setThreadOwnerEmail(String threadOwnerEmail) {
        this.threadOwnerEmail = threadOwnerEmail;
    }

    public String getThreadTitle() {
        return threadTitle;
    }

    public void setThreadTitle(String threadTitle) {
        this.threadTitle = threadTitle;
    }

    public String getThreadCategory() {
        return threadCategory;
    }

    public void setThreadCategory(String threadCategory) {
        this.threadCategory = threadCategory;
    }

    public String getThreadText() {
        return threadText;
    }

    public void setThreadText(String threadText) {
        this.threadText = threadText;
    }

    public Boolean getHasTrophy() { return hasTrophy; }

    public void setHasTrophy(Boolean hasTrophy) { this.hasTrophy = hasTrophy; }

    public String getThreadPutText() {
        return threadPutText;
    }

    public void setThreadPutText(String threadPutText) {
        this.threadPutText = threadPutText;
    }

    public Boolean getVipStatus() { return vipStatus; }

    public void setVipStatus(Boolean vipStatus) { this.vipStatus = vipStatus; }

    public Boolean getIsReported() { return isReported; }

    public void setIsReported(Boolean reported) { isReported = reported; }

    public Boolean getIsLocked() { return isLocked;}
    public void setIsLocked(Boolean isLocked) { this.isLocked = isLocked;}
}
