package com.opscontrolplane.kafka.producer;

import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.ServiceHealthEvent;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates a fleet of distributed services reporting health telemetry. Every 5 seconds
 * it picks one of the seeded services at random and publishes a plausible health sample,
 * standing in for what would otherwise be a real telemetry/metrics pipeline.
 */
@Component
@ConditionalOnProperty(name = "app.simulator.enabled", havingValue = "true", matchIfMissing = true)
public class ServiceHealthEventProducer {

    private static final Logger log = LoggerFactory.getLogger(ServiceHealthEventProducer.class);
    private static final List<String> STATUSES = List.of(
            ServiceStatus.HEALTHY.name(), ServiceStatus.HEALTHY.name(), ServiceStatus.HEALTHY.name(),
            ServiceStatus.DEGRADED.name(), ServiceStatus.OFFLINE.name());

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final DistributedServiceRepository serviceRepository;

    public ServiceHealthEventProducer(KafkaTemplate<String, Object> kafkaTemplate,
                                       DistributedServiceRepository serviceRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.serviceRepository = serviceRepository;
    }

    @Scheduled(fixedRate = 5000)
    public void publishRandomHealthEvent() {
        var services = serviceRepository.findAll();
        if (services.isEmpty()) {
            return;
        }
        var random = ThreadLocalRandom.current();
        var target = services.get(random.nextInt(services.size()));
        String status = STATUSES.get(random.nextInt(STATUSES.size()));
        double latency = switch (status) {
            case "HEALTHY" -> 20 + random.nextDouble() * 80;
            case "DEGRADED" -> 200 + random.nextDouble() * 400;
            default -> 800 + random.nextDouble() * 1200;
        };
        double errorRate = switch (status) {
            case "HEALTHY" -> random.nextDouble() * 0.01;
            case "DEGRADED" -> 0.05 + random.nextDouble() * 0.15;
            default -> 0.5 + random.nextDouble() * 0.5;
        };

        ServiceHealthEvent event = new ServiceHealthEvent(
                target.getName(), status, latency, errorRate, Instant.now(), "simulator");

        kafkaTemplate.send(KafkaTopics.SERVICE_HEALTH_EVENTS, target.getName(), event);
        log.debug("Published simulated health event for {}: status={}", target.getName(), status);
    }
}
