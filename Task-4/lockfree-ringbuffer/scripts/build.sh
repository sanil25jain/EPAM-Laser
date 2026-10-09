#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/out"

echo "Building into $OUT"
rm -rf "$OUT"
mkdir -p "$OUT"

find "$ROOT/src/main/java" -name '*.java' > "$OUT/sources.txt"
javac -d "$OUT" @"$OUT/sources.txt"
rm "$OUT/sources.txt"

echo "Build OK."
