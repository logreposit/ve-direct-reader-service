package com.logreposit.vedirectreaderservice.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class TaskExecutorConfiguration
{
    // Ensures that incoming VE.Direct events are handled FIFO
    @Bean("singleThreadPoolTaskExecutor")
    public ThreadPoolTaskExecutor singleThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setCorePoolSize(1);
        threadPoolTaskExecutor.setMaxPoolSize(1);
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
