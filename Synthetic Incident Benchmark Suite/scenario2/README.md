# Scenario 2: Cascading Third-Party Deprecation

## Overview

PayGate, ShopSphere's third-party payment gateway, gradually migrates its edge servers to a new TLS certificate chain. ShopSphere's Java-based `payment-adapter` uses a custom truststore containing only the old root certificate, causing TLS handshakes to fail with `PKIX path building failed`. These failures produce HTTP 502 errors that propagate through `checkout-service` to `api-gateway`.

The failure increases progressively as PayGate switches its four edge nodes. A recent `payment-adapter` deployment becomes the prime suspect, but rolling it back has no effect. The actual cause is the vendor-side certificate-chain change, which had been announced through emails sent to an abandoned shared mailbox. The issue is resolved by updating the truststore to include the new root certificate and rolling out the fix.

## Incident Timeline

| Time (UTC) | Event |
|---|---|
| Aug 24 / Sep 24 | PayGate sends notices to an abandoned shared mailbox |
| Oct 7, 21:14 | New leaf certificate appears in CT log |
| 08:52:08 | Decoy deployment of `payment-adapter` 2.8.1 |
| 09:00 | PayGate maintenance begins |
| 09:04 | First edge switches to the new certificate chain; PKIX errors begin |
| 09:08:30 | Datadog warning triggered |
| 09:11 / 09:17 / 09:24 | Remaining three edges switch to the new chain |
| 09:12:30 | Datadog SEV2 alert triggered |
| 09:16–09:25 | Meera rolls back version 2.8.1; errors persist |
| 09:26–09:30 | Dmitri identifies PKIX errors and per-edge failure onset |
| 09:31:40 | COD-only flag enabled as a partial mitigation |
| 09:33 | Vendor notices discovered in the abandoned mailbox |
| 09:35–09:40 | New root certificate fingerprint verified with PayGate support |
| 09:44:40 | Fix PR #2011 merged |
| 09:47–09:50 | Rolling deployment; all pods fixed by 09:49:40 |
| 10:05 | Incident resolved |

## Dataset Contents

| File | Description |
|---|---|
| `logs_raw.jsonl` | 50,000 application and infrastructure log lines |
| `datadog_metrics.csv` | One-minute metrics, including per-edge TLS failures and decoy metrics |
| `datadog_apm_traces.jsonl` | 340 sampled traces showing service calls, errors, and peer IPs |
| `datadog_events_monitors.json` | Monitor definitions, deployment events, status-page events, and alerts |
| `slack_messages.json` | 31 incident and engineering messages |
| `vendor_notices.json` | Vendor emails, abandoned-mailbox metadata, status page, and changelog |
| `cert_observations.json` | Certificate transparency records, per-edge observations, captures, and fingerprints |
| `jira_tickets.json` | Tickets documenting contributing factors and related issues |
| `git_prs.json` | Decoy PR #1998 and fix PR #2011 |
| `deploy_events.json` | Deployment events |
| `k8s/` | Kubernetes events, audit logs, before/after truststores, and Dockerfile |
| `pagerduty_alerts.json` | Incident alerts |
| `service_dependency_graph.json` | Service topology |
| `users.json` | Participant roles and seniority |
| `ground_truth.json` | Causal DAG, natural experiments, rejected hypotheses, testimony labels, and evaluation rubric; exclude from agent input |


