package com.kfokam48.relectures.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Horloge injectee : les regles de temps (RG1) deviennent testables avec une horloge fixe. */
@Configuration
public class HorlogeConfig {

    @Bean
    public Clock horloge() {
        return Clock.systemUTC();
    }
}