package com.techflow.techhubbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:RaffleControllerTest.properties")
public class RaffleControllerTestDataProperties {

    @Value("${raffle.prize}")
    private long rafflePrize;

    @Value("${raffle.drawTimeDuration}")
    private int raffleDrawTimeDuration;

    @Value("${raffle.sleepTime}")
    private long raffleSleepTime;

    public RaffleControllerTestDataProperties() {
    }

    public long getRafflePrize() {
        return rafflePrize;
    }

    public int getRaffleDrawTimeDuration() {
        return raffleDrawTimeDuration;
    }

    public long getRaffleSleepTime() {
        return raffleSleepTime;
    }
}
