/**
 * Simulates bridging a legacy SOAP-based monitoring integration into the modern
 * event-driven stack.
 *
 * Conversion flow:
 * 1. {@code POST /api/soap-adapter/simulate} accepts a SOAP-envelope-shaped XML string body,
 *    standing in for a call a legacy on-prem monitoring system might make against a SOAP
 *    web service.
 * 2. {@link com.opscontrolplane.soapadapter.SoapEnvelopeParser} extracts the handful of
 *    fields we care about (service name, status, latency, error rate) out of the envelope
 *    using lightweight XML parsing -- no WSDL/JAXB contract is generated because this is a
 *    simulation, not a real SOAP client.
 * 3. The parsed fields are mapped onto {@link com.opscontrolplane.kafka.event.ServiceHealthEvent},
 *    the same internal DTO the native Kafka health-event simulator produces.
 * 4. That event is published to the {@code service-health-events} topic, so downstream
 *    consumers (which update DistributedService state and raise alerts) treat SOAP-sourced
 *    telemetry identically to telemetry from the modern producer.
 *
 * This illustrates a common real-world pattern: an anti-corruption layer that lets a legacy
 * protocol coexist with an event-driven architecture without either side needing to change.
 */
package com.opscontrolplane.soapadapter;
