/**
 * Simulates a legacy EMS/JMS message bridge without connecting to a real EMS broker
 * (e.g. TIBCO EMS, IBM MQ). Accepts a mock JMS-shaped message over REST, normalizes it
 * into an internal event DTO, and republishes it onto the appropriate Kafka topic --
 * demonstrating how a legacy messaging integration can be bridged into an event-driven
 * architecture without requiring the legacy broker itself to be reachable.
 */
package com.opscontrolplane.emsbridge;
