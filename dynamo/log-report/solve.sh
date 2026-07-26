#!/usr/bin/env bash
set -euo pipefail

LOG_PATH="${LOG_PATH:-/app/service.log}"
REPORT_PATH="${REPORT_PATH:-/app/report.json}"
export LOG_PATH REPORT_PATH

python3 - <<'PY'
import json
from collections import Counter, defaultdict
import os
from pathlib import Path

log_path = Path(os.environ["LOG_PATH"])
report_path = Path(os.environ["REPORT_PATH"])

severity_rank = {"ERROR": 1, "CRITICAL": 2}
events = []

for line in log_path.read_text(encoding="utf-8").splitlines():
    event = json.loads(line)
    if event["level"] in severity_rank:
        events.append(event)

counts = Counter(event["service"] for event in events)
service_best_rank = defaultdict(int)
for event in events:
    service_best_rank[event["service"]] = max(
        service_best_rank[event["service"]],
        severity_rank[event["level"]],
    )

highest_service = sorted(
    counts,
    key=lambda service: (
        -service_best_rank[service],
        -counts[service],
        service,
    ),
)[0]

report = {
    "error_count_by_service": dict(sorted(counts.items())),
    "highest_severity_service": highest_service,
    "earliest_error_timestamp": min(event["timestamp"] for event in events),
    "affected_user_ids": sorted({event["user_id"] for event in events}),
}

report_path.write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
PY
