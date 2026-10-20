#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
java -cp "$ROOT/target/classes:$ROOT/target/lib/*" com.team.disruptorlib.DisruptorLibraryTest "$@"
