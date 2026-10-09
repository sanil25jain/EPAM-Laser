#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
java -cp "$ROOT/out" com.team.ringbuffer.RingBufferTest
