#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(dirname "$DIR")"

PEEK_TARGET="${1:-all}"
SERIAL="${2:-}"

EXTRA_ARGS=""
if [ -n "$SERIAL" ]; then
    EXTRA_ARGS="--serial $SERIAL"
fi

if [ "$PEEK_TARGET" = "all" ]; then
    python3 "$ROOT/tools/compare_screens.py" --all $EXTRA_ARGS
elif [ "$PEEK_TARGET" = "generate-only" ]; then
    python3 "$ROOT/tools/compare_screens.py" --generate-only
else
    python3 "$ROOT/tools/compare_screens.py" --id "$PEEK_TARGET" $EXTRA_ARGS
fi
