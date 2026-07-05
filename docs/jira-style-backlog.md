# Jira-Style Backlog - Distributed Operations Control Plane

This document simulates a project backlog/board for the portfolio project,
demonstrating familiarity with agile ticket structure, prioritization, and
cross-cutting feature planning (not a real Jira export).

## Board Summary

| ID       | Type  | Title                                                          | Description                                                                                          | Status      | Priority |
|----------|-------|------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------|-------------|----------|
| OPS-101  | Story | Implement JWT-based login endpoint                                | Add `POST /api/auth/login` issuing signed JWTs for valid credentials, backed by BCrypt password checks. | Done        | High     |
| OPS-102  | Story | Enforce role-based access control (RBAC)                         | Add method-level `@PreAuthorize` guards across controllers for ADMIN/OPERATOR/VIEWER roles.            | Done        | High     |
| OPS-103  | Task  | Seed demo users on startup                                       | Insert admin/operator/viewer seed accounts via a data initializer for local/demo environments.         | Done        | Medium   |
| OPS-104  | Bug   | Expired JWT returns 500 instead of 401                            | Global exception handler should map JWT expiration/parsing errors to a clean 401 response.             | Done        | High     |
| OPS-105  | Story | Model DistributedService inventory                                | CRUD for services under management, including health status and metadata fields.                       | Done        | High     |
| OPS-106  | Story | Publish service-health-events to Kafka                            | Emit a Kafka event to `service-health-events` whenever a service's health status changes.               | Done        | High     |
| OPS-107  | Story | Consume service-health-events to auto-generate alerts             | Kafka listener evaluates incoming health events and raises Alert entities + `alert-events` on failure.  | Done        | High     |
| OPS-108  | Story | Alert lifecycle management (ack/resolve)                          | REST endpoints to acknowledge and resolve alerts, restricted to OPERATOR/ADMIN roles.                   | Done        | Medium   |
| OPS-109  | Story | Workflow orchestration events                                     | Model multi-step operational workflows and emit `workflow-events` at each transition.                   | In Progress | Medium   |
| OPS-110  | Story | Audit trail for privileged actions                                | Persist an audit-events log for all mutating admin/operator actions; expose read-only query API.        | In Progress | Medium   |
| OPS-111  | Story | SOAP adapter simulation endpoint                                  | `POST /api/soap-adapter/simulate` parses legacy SOAP-style XML and converts it into an internal event.   | To Do       | Medium   |
| OPS-112  | Story | EMS/JMS bridge simulation endpoint                                 | `POST /api/ems-bridge/simulate` normalizes a mock EMS message into a `service-health-events` record.     | To Do       | Medium   |
| OPS-113  | Task  | Expose Actuator health/info/prometheus endpoints                  | Enable and secure `/actuator/health`, `/actuator/info`, `/actuator/prometheus` for ops tooling.          | Done        | High     |
| OPS-114  | Task  | Add Micrometer custom counters                                    | Add `alerts_created_total` and `kafka_messages_processed_total` counters for business-level observability. | In Progress | Medium   |
| OPS-115  | Task  | Provision Grafana dashboard via docker-compose                    | Ship a pre-built dashboard JSON (JVM, HTTP, custom counters) provisioned automatically on `docker compose up`. | Done  | Medium   |
| OPS-116  | Task  | Wire Prometheus scrape config for backend                         | `docker/prometheus/prometheus.yml` scrapes `backend:8080/actuator/prometheus` every 15s.                | Done        | Medium   |
| OPS-117  | Task  | Write Kubernetes manifests for all services                       | Deployments/Services/ConfigMap/Secret template/Ingress for postgres, kafka, backend, frontend.          | Done        | High     |
| OPS-118  | Task  | Add OpenShift Route + SCC documentation                           | Route objects with edge TLS termination; document non-root requirement under restricted SCC.            | Done        | Medium   |
| OPS-119  | Task  | Build Jenkins CI/CD pipeline                                       | Declarative pipeline: build/test both apps, build+push Docker images, deploy manifests via kubectl.     | Done        | High     |
| OPS-120  | Bug   | Kafka consumer group rebalances on backend restart cause dupes     | Investigate idempotent consumption / dedup strategy for `service-health-events` consumer.                | To Do       | Low      |
| OPS-121  | Story | Frontend real-time alert feed                                     | React dashboard subscribes (via polling or WebSocket) to alert state and renders a live feed.            | In Progress | Medium   |
| OPS-122  | Task  | Add ELK profile to docker-compose for log aggregation              | Optional `elk` compose profile bringing up Elasticsearch + Kibana for structured JSON log viewing.       | Done        | Low      |

## Notes on Prioritization

- **High priority** tickets cover the auth/RBAC foundation, the core Kafka
  health-to-alert pipeline, and deployment/CI readiness - these are the
  tickets that most directly demonstrate distributed-systems and platform
  engineering competency.
- **Medium priority** tickets round out the secondary integration surfaces
  (SOAP adapter, EMS bridge, workflows, audit trail) and observability
  polish.
- **Low priority** tickets are stretch/hardening items (log aggregation,
  consumer idempotency) that would matter more in a production system than
  in a portfolio demo, but are included to show awareness of the concerns.
