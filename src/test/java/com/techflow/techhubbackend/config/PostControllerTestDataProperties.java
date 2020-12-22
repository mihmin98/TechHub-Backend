package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Set;

@Configuration
@PropertySource("classpath:PostControllerTest.properties")
public class PostControllerTestDataProperties {

    @Value("${post.userEmail}")
    private String postUserEmail;

    @Value("${post.text}")
    private String postText;

    @Value("${post.hasTrophy}")
    private Boolean postHasTrophy;

    @Value("${post.upvotes}")
    private Set<String> upvotes;

    @Value("${post.downvotes}")
    private Set<String> downvotes;

    @Value("${post.put.text}")
    private String postPutText;

    public PostControllerTestDataProperties() {
    }

    public String getPostUserEmail() {
        return postUserEmail;
    }

    public void setPostUserEmail(String postUserEmail) {
        this.postUserEmail = postUserEmail;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public Boolean getPostHasTrophy() {
        return postHasTrophy;
    }

    public void setPostHasTrophy(Boolean postHasTrophy) {
        this.postHasTrophy = postHasTrophy;
    }

    public Set<String> getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(Set<String> upvotes) {
        this.upvotes = upvotes;
    }

    public Set<String> getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(Set<String> downvotes) {
        this.downvotes = downvotes;
    }

    public String getPostPutText() {
        return postPutText;
    }

    public void setPostPutText(String postPutText) {
        this.postPutText = postPutText;
    }
}
