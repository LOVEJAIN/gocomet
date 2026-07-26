import json
import os
from pathlib import Path


REPORT_PATH = Path(os.environ.get("REPORT_PATH", "/app/report.json"))
EXPECTED_PATH = Path(os.environ.get("EXPECTED_PATH", "/tests/expected_report.json"))


def load_report():
    assert REPORT_PATH.exists(), f"Missing expected artifact: {REPORT_PATH}"
    return json.loads(REPORT_PATH.read_text(encoding="utf-8"))


def expected_report():
    return json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))


def test_report_exists_and_is_json_object():
    """Verifies success criterion 1 from instruction.md."""
    report = load_report()
    assert isinstance(report, dict)


def test_error_count_by_service_matches_expected_failures():
    """Verifies success criterion 2 from instruction.md."""
    report = load_report()
    expected = expected_report()
    assert report.get("error_count_by_service") == expected["error_count_by_service"]


def test_highest_severity_service_uses_required_tie_breaking():
    """Verifies success criterion 3 from instruction.md."""
    report = load_report()
    expected = expected_report()
    assert report.get("highest_severity_service") == expected["highest_severity_service"]


def test_timestamp_and_user_list_match_expected_failure_events():
    """Verifies success criterion 4 from instruction.md."""
    report = load_report()
    expected = expected_report()
    assert report.get("earliest_error_timestamp") == expected["earliest_error_timestamp"]
    assert report.get("affected_user_ids") == expected["affected_user_ids"]
