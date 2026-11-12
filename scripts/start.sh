#!/usr/bin/env bash
# Starts DotProxy and restarts it automatically when it asks to be restarted.
# Place this script next to DotProxy.jar. Requires Java 21+ on the PATH (or set JAVA).
# Exit code 69 = intentional shutdown (no restart).
cd "$(dirname "$0")" || exit 1
JAVA="${JAVA:-java}"
JVM_ARGS="${JVM_ARGS:--Xmx300m -Xms32m -XX:+UseG1GC -XX:MinHeapFreeRatio=10 -XX:MaxHeapFreeRatio=20 -XX:G1PeriodicGCInterval=30000}"
while true; do
  # shellcheck disable=SC2086
  "$JAVA" $JVM_ARGS --enable-native-access=ALL-UNNAMED -jar DotProxy.jar "$@"
  code=$?
  if [ "$code" -eq 69 ]; then
    echo "DotProxy shut down."
    exit 0
  fi
  echo "DotProxy exited with code $code, restarting in 3 seconds... (Ctrl+C to stop)"
  sleep 3
done
