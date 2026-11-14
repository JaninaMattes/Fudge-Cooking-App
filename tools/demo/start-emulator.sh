#!/usr/bin/env bash
#
# Starts an Android emulator and waits until the system is ready.
#
# The script does nothing when a device already runs, so "make demo-video"
# can call it in each case.
#
# Usage:
#   ./tools/demo/start-emulator.sh [name of the AVD]
#
# Exit codes:
#   0  a device runs
#   1  the SDK, the emulator or the AVD is missing

set -euo pipefail

readonly AVD_NAME="${1:-fudge-34}"
readonly SDK="${ANDROID_HOME:-$HOME/Library/Android/sdk}"
readonly EMULATOR="${SDK}/emulator/emulator"
readonly ADB="${SDK}/platform-tools/adb"
readonly BOOT_TIMEOUT="${BOOT_TIMEOUT:-300}"

log() { printf '\033[0;36m[emulator]\033[0m %s\n' "$*"; }
fail() { printf '\033[0;31m[emulator]\033[0m %s\n' "$*" >&2; exit 1; }

[[ -x "${ADB}" ]] || fail "adb is missing. Set ANDROID_HOME to the Android SDK."
[[ -x "${EMULATOR}" ]] || fail "The package 'emulator' is missing. Run 'make emulator-create'."

if "${ADB}" devices | grep -qw device; then
  log "A device already runs: $("${ADB}" shell getprop ro.product.model | tr -d '\r')"
  exit 0
fi

"${EMULATOR}" -list-avds | grep -qx "${AVD_NAME}" \
  || fail "The AVD '${AVD_NAME}' is missing. Run 'make emulator-create'."

log "Starting ${AVD_NAME}"
# The emulator keeps running after this script ends, so the next command of
# the demo finds it. The log goes into a file, because the output of the
# emulator is long.
"${EMULATOR}" -avd "${AVD_NAME}" -no-boot-anim -no-snapshot-save \
  > "${TMPDIR:-/tmp}/fudge-emulator.log" 2>&1 &

"${ADB}" wait-for-device

log "Waiting for the end of the start (at most ${BOOT_TIMEOUT} s)"
for ((second = 0; second < BOOT_TIMEOUT; second += 2)); do
  if [[ "$("${ADB}" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; then
    log "The emulator is ready: $("${ADB}" shell getprop ro.product.model | tr -d '\r')"
    exit 0
  fi
  sleep 2
done

fail "The emulator did not start in ${BOOT_TIMEOUT} s. See ${TMPDIR:-/tmp}/fudge-emulator.log."
