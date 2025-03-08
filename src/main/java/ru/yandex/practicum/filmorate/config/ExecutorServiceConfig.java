package ru.yandex.practicum.filmorate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorServiceConfig {

    @Bean
    public ExecutorService executorService() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Executors.newFixedThreadPool(availableProcessors);
    }
}
