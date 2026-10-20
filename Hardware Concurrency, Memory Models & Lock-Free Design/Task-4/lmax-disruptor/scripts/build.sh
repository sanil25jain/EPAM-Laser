#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "Building (Maven downloads the Disruptor jar on first run)..."
mvn -q clean package
mvn -q dependency:copy-dependencies -DoutputDirectory=target/lib

echo "Build OK."
