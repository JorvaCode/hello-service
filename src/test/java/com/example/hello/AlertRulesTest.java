package com.example.hello;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the alert-rule definitions for IMP-004. The rules are configuration
 * evidence, not a running Prometheus deployment.
 */
class AlertRulesTest {

    private static final Path RULES = Path.of("ops/prometheus/hello-service-alerts.yml");

    private static final Set<String> EXPECTED_ALERTS = Set.of(
            "HelloServiceDown",
            "HelloServiceHigh5xxRate",
            "HelloServiceLatencyHigh",
            "HelloServiceRestarting",
            "HelloServiceLivenessProbeFailing",
            "HelloServiceReadinessProbeFailing",
            "HelloServiceHelloContractFailing",
            "HelloServiceCorrelationMissing");

    @Test
    void rulesCoverFailureModesAndNameResponse() throws Exception {
        Map<String, Object> root;
        try (InputStream input = Files.newInputStream(RULES)) {
            root = new Yaml().load(input);
        }

        List<Map<String, Object>> groups = listOfMaps(root.get("groups"));
        assertThat(groups).hasSize(1);

        List<Map<String, Object>> rules = listOfMaps(groups.get(0).get("rules"));
        Set<String> alerts = rules.stream()
                .map(rule -> (String) rule.get("alert"))
                .collect(Collectors.toSet());

        assertThat(alerts).isEqualTo(EXPECTED_ALERTS);

        for (Map<String, Object> rule : rules) {
            String alert = (String) rule.get("alert");

            assertThat(rule.get("expr")).as("%s expr", alert).isNotNull();
            assertThat(rule.get("for")).as("%s for", alert).isNotNull();

            Map<String, Object> labels = mapOf(rule.get("labels"));
            assertThat(labels.get("severity")).as("%s severity", alert)
                    .isIn("warning", "critical");
            assertThat(labels.get("service")).as("%s service", alert)
                    .isEqualTo("hello-service");

            Map<String, Object> annotations = mapOf(rule.get("annotations"));
            assertThat(annotations.get("summary")).as("%s summary", alert).isNotNull();
            assertThat(annotations.get("description")).as("%s description", alert).isNotNull();
            assertThat(annotations.get("runbook_url")).as("%s runbook", alert)
                    .asString()
                    .contains("blueprint/observability/hello-service.md#rb-");
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listOfMaps(Object value) {
        return (List<Map<String, Object>>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapOf(Object value) {
        return (Map<String, Object>) value;
    }
}
