package com.rapidos.despacho.infraestructura.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration(proxyBeanMethods = false)
@EnableAsync
class DespachoAsyncConfig {

    @Bean
    ThreadPoolTaskExecutor taskExecutor(
            @Value("${rapidos.workers.core-size}") int coreSize,
            @Value("${rapidos.workers.max-size}") int maxSize,
            @Value("${rapidos.workers.queue-capacity}") int queueCapacity) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("despacho-worker-");
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }
}
