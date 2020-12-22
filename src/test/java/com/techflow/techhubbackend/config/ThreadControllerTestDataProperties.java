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

    @Value("${thread.put.text}")
    private String threadPutText;

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

    public String getThreadPutText() {
        return threadPutText;
    }

    public void setThreadPutText(String threadPutText) {
        this.threadPutText = threadPutText;
    }
}
