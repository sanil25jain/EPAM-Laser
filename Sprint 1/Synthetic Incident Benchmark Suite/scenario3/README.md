# Scenario 3: Database Connection Leak

## Overview

A release of `checkout-service` version `3.18.0` (PR #2042) introduces a JDBC connection leak in `OrderReservationRepository`'s retry loop. When an exception occurs, the cleanup path skips `close()`, causing active connections and old `idle_in_transaction` sessions to accumulate gradually over approximately four hours.

Eventually, `HikariPool-Checkout` reaches its limit of 80 active connections, increasing connection-acquisition waits and causing orders persistence failures, followed by checkout and API gateway timeouts. PostgreSQL CPU usage and lock waits remain modest, while a campaign traffic increase and reporting-index change serve as plausible decoys. The issue is fixed in version `3.18.1` (PR #2054), which closes connections through a `finally` block. Rolling pod replacement drains leaked sessions and restores service.

## Incident Timeline

| Time (UTC) | Event |
|---|---|
| Oct 12 | PR #2042 introduces the retry loop; tests omit repeated-exception resource cleanup |
| 08:00 | Scenario telemetry begins; campaign worker concurrency change acts as a decoy |
| 08:18 | Active database connections begin sustained upward drift |
| 10:26 | Retry exception path logs skipped connection cleanup |
| 11:08 | First pool-acquisition timeout and checkout errors |
| 11:13 | SEV1 alert triggered; team investigates traffic and reporting-query hypotheses |
| 11:25 | Active sessions reach pool saturation; old idle-in-transaction sessions accumulate |
| 11:42 | Old checkout sessions and retry exception path identified |
| 11:51 | Fix PR #2054 merged; rollout begins |
| 11:58 | Version 3.18.1 rollout completes; new leaks stop and old sessions drain |
| 12:09 | Error rate recovers |
| 12:11 | Incident resolved |

## Dataset Contents

| File | Description |
|---|---|
| `incident.json` | Incident metadata, affected services, time window, and summary |
| `logs_raw.jsonl` | 50,000 structured log lines containing incident signals and healthy noise |
| `datadog_metrics.csv` | One-minute production and staging metrics covering pool usage, waits, symptoms, and decoys |
| `datadog_apm_traces.jsonl` | 340 sampled traces showing request propagation and failures across services |
| `datadog_events_monitors.json` | Monitor definitions and events; leak-age monitor intentionally disabled |
| `slack_messages.json` | Incident discussion, competing explanations, and corrections |
| `jira_tickets.json` | Connection-cleanup warnings, incident tickets, follow-ups, and decoy tickets |
| `git_prs.json` | Causal PR #2042, decoy PR #2051, fix PR #2054, and campaign concurrency change |
| `deploy_events.json` | Release and remediation events |
| `k8s/k8s_events.jsonl` | Kubernetes readiness and rollout events |
| `k8s/k8s_audit.jsonl` | Kubernetes audit events |
| `pagerduty_alerts.json` | SEV1 alert, updates, and recovery |
| `service_dependency_graph.json` | Service topology |
| `users.json` | Participant roles and seniority |
| `ground_truth.json` | Causal DAG, evidence links, rejected hypotheses, testimony labels, and evaluation rubric; exclude from agent input |
| `MANIFEST.json` | File sizes, SHA-256 digests, record counts, and generation seed |

