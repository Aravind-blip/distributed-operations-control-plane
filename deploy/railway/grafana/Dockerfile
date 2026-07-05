# Railway-specific Grafana image: bakes in provisioning pointed at the
# Prometheus service's Railway private-network hostname
# (prometheus.railway.internal) instead of the docker-compose service name
# used locally.
FROM grafana/grafana:10.4.0
COPY datasource.yml /etc/grafana/provisioning/datasources/prometheus.yml
COPY dashboard-provider.yml /etc/grafana/provisioning/dashboards/dashboard.yml
COPY ops-control-plane-dashboard.json /var/lib/grafana/dashboards/ops-control-plane-dashboard.json
