package com.opscontrolplane.kafka;

import com.opscontrolplane.kafka.event.ServiceHealthEvent;
import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.Environment;
import com.opscontrolplane.services.ServiceStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, topics = {KafkaTopics.SERVICE_HEALTH_EVENTS, KafkaTopics.ALERT_EVENTS,
        KafkaTopics.WORKFLOW_EVENTS, KafkaTopics.AUDIT_EVENTS})
class ServiceHealthEventConsumerIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private DistributedServiceRepository serviceRepository;

    @Test
    void healthEventPublishedToTopicUpdatesServiceStatusAndLatency() {
        DistributedService service = serviceRepository.save(new DistributedService(
                "Kafka Test Service", Environment.CLOUD, "us-east-1", ServiceStatus.HEALTHY, 40.0, 0.001,
                Instant.now(), "1.0.0", "Test Team", "REST API"));

        ServiceHealthEvent event = new ServiceHealthEvent(
                "Kafka Test Service", "DEGRADED", 550.5, 0.2, Instant.now(), "test");

        kafkaTemplate.send(KafkaTopics.SERVICE_HEALTH_EVENTS, "Kafka Test Service", event);

        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            DistributedService updated = serviceRepository.findById(service.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(ServiceStatus.DEGRADED);
            assertThat(updated.getLatencyMs()).isEqualTo(550.5);
        });
    }
}
