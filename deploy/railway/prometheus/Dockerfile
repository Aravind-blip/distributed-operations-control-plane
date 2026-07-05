# Railway-specific Prometheus image: bakes in a config pointed at the
# backend's Railway private-network hostname (backend.railway.internal)
# instead of the docker-compose service name used locally.
FROM prom/prometheus:v2.51.0
COPY prometheus.yml /etc/prometheus/prometheus.yml
