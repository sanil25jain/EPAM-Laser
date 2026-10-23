# Scenario 1: The Bad Config Push

## Overview

A malformed production ConfigMap value, `CHECKOUT_DB_POOL_MAX=5O` (letter `O` instead of zero), causes the checkout application's configuration parser to fail. All six pods enter `CrashLoopBackOff`, resulting in gateway `503/504` errors and increased CPU usage.

The incident is enabled by a branch-protection bypass, skipped code review and QA, and a CI validation job skipped due to a path-filter bug. Staging remains healthy because it uses the correct value, misleading the on-call engineer. Decoy events, conflicting engineer statements, and an ineffective `rollout undo` complicate the investigation. Service is restored by reverting the configuration and replacing the affected pods.

## Incident Timeline

| Time (UTC) | Event |
|---|---|
| 13:58 | Code review requested from CODEOWNER |
| 14:09:40 | PR #1893 merged with approval bypass |
| 14:11:32 | ArgoCD syncs faulty ConfigMap revision 42 |
| 14:12:41 | First pod crashes due to configuration parsing failure |
| 14:14:07 | 0/6 checkout pods ready |
| 14:15:22 | SEV2 alert triggered |
| 14:17–14:31 | On-call investigates staging instead of production |
| 14:34 | `rollout undo` has no effect |
| 14:40–14:43 | Revert PR #1896 and configuration sync |
| 14:44:35 | All six pods ready |
| 14:50 | Incident resolved |

## Dataset Contents

| File | Description |
|---|---|
| `logs_raw.jsonl` | 50,000 application and infrastructure log lines |
| `metrics.csv` | One-minute production and staging metrics |
| `slack_messages.json` | 38 incident and engineering messages |
| `jira_tickets.json` | Incident tickets, override history, and contributing factors |
| `git_prs.json` | Faulty PR, revert PR, and unrelated change |
| `deploy_events.json` | CI and ArgoCD events |
| `k8s/` | Kubernetes events, audit logs, and ConfigMap revisions |
| `pagerduty_alerts.json` | Incident and symptom alerts |
| `service_dependency_graph.json` | Service topology |
| `users.json` | Participant roles and seniority |
| `ground_truth.json` | Causal DAG, rejected hypotheses, testimony labels, and evaluation rubric |