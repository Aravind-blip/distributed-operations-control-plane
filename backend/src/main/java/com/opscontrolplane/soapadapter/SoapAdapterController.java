package com.opscontrolplane.soapadapter;

import com.opscontrolplane.kafka.KafkaTopics;
import com.opscontrolplane.kafka.event.ServiceHealthEvent;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * See package-info.java for the full SOAP-to-Kafka conversion flow this endpoint
 * demonstrates. This simulates a legacy SOAP integration being bridged into the
 * modern event-driven stack -- no real SOAP server or WSDL is involved.
 */
@RestController
@RequestMapping("/api/soap-adapter")
public class SoapAdapterController {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SoapAdapterController(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostMapping(value = "/simulate", consumes = {MediaType.TEXT_XML_VALUE, MediaType.APPLICATION_XML_VALUE,
            MediaType.TEXT_PLAIN_VALUE, "application/soap+xml"})
    public Map<String, Object> simulate(@RequestBody String soapEnvelopeXml) {
        SoapEnvelopeParser.ParsedHealthPayload parsed = SoapEnvelopeParser.parse(soapEnvelopeXml);

        ServiceHealthEvent event = new ServiceHealthEvent(
                parsed.serviceName(), parsed.status().toUpperCase(), parsed.latencyMs(), parsed.errorRate(),
                Instant.now(), "soap-adapter");

        kafkaTemplate.send(KafkaTopics.SERVICE_HEALTH_EVENTS, parsed.serviceName(), event);

        return Map.of(
                "received", true,
                "convertedEvent", event,
                "publishedTopic", KafkaTopics.SERVICE_HEALTH_EVENTS);
    }
}
