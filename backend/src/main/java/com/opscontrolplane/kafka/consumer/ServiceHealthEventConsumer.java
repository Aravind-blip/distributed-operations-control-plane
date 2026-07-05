package com.opscontrolplane.kafka.consumer;

import com.opscontrolplane.alerts.Alert;
import com.opscontrolplane.alerts.AlertRepository;
import com.opscontrolplane.alerts.AlertSeverity;
import com.opscontrolplane.alerts.AlertStatus;
import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.AlertEvent;
import com.opscontrolplane.kafka.event.ServiceHealthEvent;
import com.opscontrolplane.metrics.OpsMetrics;
import com.opscontrolplane.services.DistributedService;
import com.opscontrolplane.services.DistributedServiceRepository;
import com.opscontrolplane.services.ServiceStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class ServiceHealthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ServiceHealthEventConsumer.class);

    private final DistributedServiceRepository serviceRepository;
    private final AlertRepository alertRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final OpsMetrics metrics;

    public ServiceHealthEventConsumer(DistributedServiceRepository serviceRepository,
                                       AlertRepository alertRepository,
                                       KafkaTemplate<String, Object> kafkaTemplate,
                                       OpsMetrics metrics) {
        this.serviceRepository = serviceRepository;
        this.alertRepository = alertRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.metrics = metrics;
    }

    @KafkaListener(topics = KafkaTopics.SERVICE_HEALTH_EVENTS, groupId = "ops-control-plane-health")
    @Transactional
    public void onHealthEvent(ServiceHealthEvent event) {
        try {
            List<DistributedService> matches = serviceRepository.findByNameIgnoreCase(event.serviceName());
            if (matches.isEmpty()) {
                log.warn("Received health event for unknown service '{}'", event.serviceName());
                metrics.kafkaMessagesFailed().increment();
                return;
            }
            DistributedService service = matches.get(0);
            ServiceStatus previousStatus = service.getStatus();

            service.setStatus(ServiceStatus.valueOf(event.status()));
            service.setLatencyMs(event.latencyMs());
            service.setErrorRate(event.errorRate());
            service.setLastHeartbeat(event.observedAt() != null ? event.observedAt() : Instant.now());
            serviceRepository.save(service);

            if (service.getStatus() != previousStatus
                    && (service.getStatus() == ServiceStatus.DEGRADED || service.getStatus() == ServiceStatus.OFFLINE)) {
                createAlertForDegradation(service);
            }

            metrics.kafkaMessagesProcessed().increment();
        } catch (Exception ex) {
            metrics.kafkaMessagesFailed().increment();
            log.error("Failed to process service health event for {}", event.serviceName(), ex);
        }
    }

    private void createAlertForDegradation(DistributedService service) {
        AlertSeverity severity = service.getStatus() == ServiceStatus.OFFLINE ? AlertSeverity.CRITICAL : AlertSeverity.HIGH;
        Alert alert = new Alert(
                severity,
                service.getId(),
                service.getName() + " is " + service.getStatus(),
                "Automated alert: health event pushed " + service.getName() + " into " + service.getStatus()
                        + " (latency=" + service.getLatencyMs() + "ms, errorRate=" + service.getErrorRate() + ")",
                AlertStatus.OPEN);
        Alert saved = alertRepository.save(alert);

        AlertEvent alertEvent = new AlertEvent(
                saved.getId(), saved.getSourceServiceId(), saved.getSeverity().name(),
                saved.getStatus().name(), saved.getTitle(), Instant.now());
        kafkaTemplate.send(KafkaTopics.ALERT_EVENTS, saved.getId().toString(), alertEvent);
    }
}
