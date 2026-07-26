Read `/app/service.log`, which contains one JSON object per line describing application events.

Write your final answer to `/app/report.json` as UTF-8 encoded JSON.

1. Create `/app/report.json` and make it a valid JSON object.
2. Include an `error_count_by_service` object that counts only events whose `level` is `ERROR` or `CRITICAL`, grouped by `service`.
3. Include a `highest_severity_service` string. Rank severities as `CRITICAL` higher than `ERROR`; if multiple services share the same highest severity, break ties by the larger failure count and then alphabetically by service name.
4. Include an `earliest_error_timestamp` string for the earliest event counted in step 2, and an `affected_user_ids` array containing the unique `user_id` values from the counted events sorted in ascending order.
