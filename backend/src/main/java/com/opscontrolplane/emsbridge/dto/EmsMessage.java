package com.opscontrolplane.emsbridge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

/**
 * Shape of a mock EMS/JMS-style message. destination mimics a JMS queue/topic name
 * (e.g. "queue/workflow.requests" or "topic/service.health"); the bridge inspects it
 * to decide which internal Kafka topic the normalized event should land on.
 */
public record EmsMessage(
        @NotBlank String messageId,
        @NotBlank String destination,
        @NotNull Object payload,
        Instant timestamp
) {
}
