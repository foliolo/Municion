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

The pre-v3.3 production rules (snapshot 2026-05-13) are saved in
`docs/firebase_rules_rollback.json`. To roll back:

```bash
# 1. Backup the currently-deployed rules first (in case you need to
#    re-apply them later).
firebase database:get "/.settings/rules" --project municion-95caa \
    > /tmp/rules_before_rollback.json

# 2. Apply the rollback. Easiest path: temporarily replace the active
#    rules file the firebase CLI deploys.
cp database.rules.json database.rules.json.bak
cp docs/firebase_rules_rollback.json database.rules.json
firebase deploy --only database --project municion-95caa

# 3. Restore the working tree (the rollback rules are in Firebase now,
#    the v3.3 rules are back in your working tree).
mv database.rules.json.bak database.rules.json
```

⚠️ The rollback rules have a known privacy weakness — `users/.read: true`
exposes every user's tree to any authenticated user. Use them only as a
short-lived recovery; re-deploy `database.rules.json` (phase-1) as soon
as the cause of the rollback is understood.
