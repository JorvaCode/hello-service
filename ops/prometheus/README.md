# Prometheus rules for hello-service

These files are configuration evidence for `IMP-001`, `IMP-003`, `IMP-004` and
`IMP-005`. They are not a running monitoring stack: no Prometheus, Alertmanager or
blackbox exporter instance is part of this repository or the local PoC.

## Scrape configuration

Add the application target to `prometheus.yml`:

```yaml
scrape_configs:
  - job_name: hello-service
    metrics_path: /actuator/prometheus
    static_configs:
      - targets: ["hello-service:8080"]
```

The `/actuator/prometheus` endpoint is exposed by `micrometer-registry-prometheus`
and carries the common tag `application="hello-service"`.

## Probe configuration

The liveness, readiness and contract alerts use the blackbox exporter. The probes
must use these job names for the rules to match:

```yaml
scrape_configs:
  - job_name: hello-service-liveness
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets: ["http://hello-service:8080/actuator/health/liveness"]
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115

  - job_name: hello-service-readiness
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets: ["http://hello-service:8080/actuator/health/readiness"]
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115

  - job_name: hello-service-hello
    metrics_path: /probe
    params:
      module: [http_2xx]
    static_configs:
      - targets: ["http://hello-service:8080/api/hello"]
    relabel_configs:
      - source_labels: [__address__]
        target_label: __param_target
      - source_labels: [__param_target]
        target_label: instance
      - target_label: __address__
        replacement: blackbox-exporter:9115
```

`HelloServiceHelloContractFailing` checks the status code only. The exact
`Hello, World!` body remains a manual check in
[RB-03](../../blueprint/observability/hello-service.md#rb-03).

## Load the rules

```yaml
rule_files:
  - /etc/prometheus/rules/hello-service-alerts.yml
```

Validate the file before loading it:

```bash
promtool check rules ops/prometheus/hello-service-alerts.yml
```

Alert delivery, inhibition and routing belong to the Alertmanager configuration and
are outside this repository's PoC scope.
