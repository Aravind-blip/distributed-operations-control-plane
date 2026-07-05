package com.opscontrolplane.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic serviceHealthEventsTopic() {
        return TopicBuilder.name(KafkaTopics.SERVICE_HEALTH_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic alertEventsTopic() {
        return TopicBuilder.name(KafkaTopics.ALERT_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic workflowEventsTopic() {
        return TopicBuilder.name(KafkaTopics.WORKFLOW_EVENTS).partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name(KafkaTopics.AUDIT_EVENTS).partitions(3).replicas(1).build();
    }
}
