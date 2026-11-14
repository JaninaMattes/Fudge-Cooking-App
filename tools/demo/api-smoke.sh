#!/bin/sh
# Asks the running REST API the three questions that the demo depends on.
#
#   1. Can the demo user log in?
#   2. Does the pantry hold products that expire soon?
#   3. Does the recipe list hold at least three recipes?
#
# Without these three answers every screen of the demo stays empty, and the
# cause is hard to see on a telephone. The script says it in three lines.
#
# Usage:
#   make api-smoke
#   BASE=http://10.0.2.2:80 USER_ID=2 make api-smoke
#
# It needs curl only. Each answer is read with grep, so the machine needs no
# tool for JSON.

set -u

BASE="${BASE:-http://localhost}"
USER_ID="${USER_ID:-1}"
EMAIL="${EMAIL:-john.smith@testmail.com}"
PASSWORD="${PASSWORD:-testpassword}"
MIN_RECIPES="${MIN_RECIPES:-3}"

failed=0

ok() {
    printf '  ok    %s\n' "$1"
}

fail() {
    printf '  FAIL  %s\n' "$1"
    failed=1
}

printf 'Smoke test of the API at %s, for the user %s\n' "$BASE" "$USER_ID"

# 1. The login. Without it the client never leaves the first screen.
token=$(curl -s -S -m 10 -X POST "$BASE/api/users/login" \
    -H 'Content-Type: application/json' \
    -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}" 2>/dev/null \
    | grep -o '"token"' | head -1)
if [ -n "$token" ]; then
    ok "the demo user can log in"
else
    fail "the login gives no token. Is the API up? Try: make api-logs"
fi

# 2. The products that expire soon. They carry the dashboard.
count=$(curl -s -S -m 10 "$BASE/api/products/count/expiring/$USER_ID" 2>/dev/null \
    | tr -dc '0-9')
if [ -n "$count" ] && [ "$count" -gt 0 ]; then
    ok "$count products expire soon"
else
    fail "no product expires soon. The dashboard would stay empty."
fi

# 3. The recipes. The list reads the database, so it needs no key of an
#    external service.
recipes=$(curl -s -S -m 10 "$BASE/api/recipe/rest/recipes/$USER_ID" 2>/dev/null \
    | grep -o '"title"' | wc -l | tr -dc '0-9')
if [ -n "$recipes" ] && [ "$recipes" -ge "$MIN_RECIPES" ]; then
    ok "$recipes recipes come back"
else
    fail "only ${recipes:-0} recipes come back, and the demo needs $MIN_RECIPES"
fi

if [ "$failed" -eq 0 ]; then
    printf 'The API is ready for the demo.\n'
else
    printf 'The API is NOT ready for the demo.\n'
fi

exit "$failed"
