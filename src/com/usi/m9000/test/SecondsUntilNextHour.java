package com.usi.m9000.test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class SecondsUntilNextHour {

    public static void main(String[] args) {
        // Get the current time
        LocalDateTime currentTime = LocalDateTime.now();

        // Calculate the seconds until the start of the next hour
        long secondsUntilNextHour = calculateSecondsUntilNextHour(currentTime);

        System.out.println("Seconds until the start of the next hour: " + secondsUntilNextHour + " seconds");
    }

    private static long calculateSecondsUntilNextHour(LocalDateTime currentTime) {
        LocalDateTime nextHour = currentTime.plusHours(1).withMinute(0).withSecond(0).withNano(0);

        // Calculate the difference in seconds
        return ChronoUnit.SECONDS.between(currentTime, nextHour);
    }
}

