# Firebase RTDB Rules — Deploy Guide

The new rules live in `database.rules.json`, deployed via Firebase CLI.

## Pre-flight check

```bash
# Show currently-deployed rules (you will need write access to the project).
firebase database:get "/.settings/rules.json" --project municion-95caa
```

## Validate locally

```bash
firebase deploy --only database --dry-run --project municion-95caa
```

## Deploy

```bash
firebase deploy --only database --project municion-95caa
```

Apply only after the v3.3.0 client is rolled out to the majority of users.
The rules in this repo are intentionally **phase-1** — they enforce per-user
isolation and the most dangerous field constraints (cupo > 0, unidades > 0,
deleted must be boolean) without breaking legacy v3.2.x writers.

## Phase 2 (future) — tighten after adoption

Once telemetry shows >95% of writes are on v3.3.0+, replace each `"$key"`
with `"$syncId": { ".validate": "$syncId.length >= 16", ... }` and require
the payload `syncId` to equal the key (instead of allowing null). See
`docs/SYNC_REDESIGN.md` §4.7 for the target shape.

## Emergency rollback

```bash
# Restore the previous rules from a local backup (the CLI keeps one if
# you ran deploy from this same machine). Otherwise pull from console.
firebase database:set / --project municion-95caa /path/to/backup.json
firebase deploy --only database --project municion-95caa  # ← with old rules
```
