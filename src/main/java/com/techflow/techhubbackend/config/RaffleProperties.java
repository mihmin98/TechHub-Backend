package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:raffle.properties")
public class RaffleProperties {

    @Value("raffle.daysBetweenRaffles")
    private Integer daysBetweenRaffles;

    @Value("raffle.entryCost")
    private Integer entryCost;

    @Value("raffle.winningsPercentage")
    private Double winningsPercentage;

    public Integer getDaysBetweenRaffles() {
        return daysBetweenRaffles;
    }

    public Integer getEntryCost() {
        return entryCost;
    }

    public Double getWinningsPercentage() {
        return winningsPercentage;
    }
}
