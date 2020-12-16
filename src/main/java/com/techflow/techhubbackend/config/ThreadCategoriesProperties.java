package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Configuration
@PropertySource("classpath:threadCategories.properties")
public class ThreadCategoriesProperties {

    @Value("#{${thread.categories}}")
    private List<String> categories;

    public ThreadCategoriesProperties() {
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
}
