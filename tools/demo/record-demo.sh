#!/usr/bin/env bash
#
# Records a video of the Fudge application on an Android emulator.
#
# The script needs no telephone. It starts an emulator, installs the debug
# APK, does the steps of the demo and writes an MP4 file.
#
# It can work in two modes:
#
#   maestro  A Maestro flow does the steps. Maestro writes the video itself
#            and puts the name of each step into the picture. Use this mode
#            for a demo video that other people watch.
#            See tools/demo/flows/.
#
#   adb      The Android tools do all the work: "adb shell screenrecord"
#            records the screen, and the test of the user interface
#            (androidTest) does the steps. This mode needs no extra tool.
#
# Examples:
#   ./tools/demo/record-demo.sh                 # mode maestro, if available
#   MODE=adb ./tools/demo/record-demo.sh        # mode adb
#   DEVICE=emulator-5554 ./tools/demo/record-demo.sh
#
# Exit codes:
#   0  the script wrote the video
#   1  a program or a device is missing
#   2  the steps of the demo failed

set -euo pipefail

# --- Settings ---------------------------------------------------------------

readonly PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
readonly FRONTEND_DIR="${PROJECT_ROOT}/frontend/android-feedme-app"
readonly FLOW_FILE="${PROJECT_ROOT}/tools/demo/flows/demo.yaml"
readonly OUTPUT_DIR="${OUTPUT_DIR:-${PROJECT_ROOT}/build/demo}"
readonly APP_ID="com.mobilesystems.feedme"

# The recording of the Android tools stops after three minutes.
readonly MAX_RECORD_SECONDS="${MAX_RECORD_SECONDS:-180}"
readonly MODE="${MODE:-auto}"
readonly DEVICE="${DEVICE:-}"

# --- Helpers ----------------------------------------------------------------

log() { printf '\033[0;36m[demo]\033[0m %s\n' "$*"; }
warn() { printf '\033[0;33m[demo]\033[0m %s\n' "$*" >&2; }
fail() { printf '\033[0;31m[demo]\033[0m %s\n' "$*" >&2; exit "${2:-1}"; }

has() { command -v "$1" >/dev/null 2>&1; }

adb_cmd() {
  if [[ -n "${DEVICE}" ]]; then
    adb -s "${DEVICE}" "$@"
  else
    adb "$@"
  fi
}

# --- Tests of the environment -----------------------------------------------

check_tools() {
  has adb || fail "adb is missing. Install the Android SDK platform tools."

  local devices
  devices="$(adb devices | grep -cw 'device' || true)"
  if [[ "${devices}" -eq 0 ]]; then
    fail "No emulator and no telephone. Start an emulator first:
  emulator -list-avds
  emulator -avd <name> &
  adb wait-for-device"
  fi
  log "Device found: $(adb_cmd shell getprop ro.product.model | tr -d '\r')"
}

build_and_install() {
  log "Building the debug APK"
  (cd "${FRONTEND_DIR}" && ./gradlew --console=plain assembleDebug)

  local apk
  apk="$(find "${FRONTEND_DIR}/app/build/outputs/apk/debug" -name '*.apk' | head -1)"
  [[ -n "${apk}" ]] || fail "The build wrote no APK file."

  log "Installing ${apk##*/}"
  adb_cmd install -r -t "${apk}" >/dev/null
}

prepare_screen() {
  # The animations make the video unsteady, so the script stops them.
  log "Stopping the animations of the system"
  adb_cmd shell settings put global window_animation_scale 0 || true
  adb_cmd shell settings put global transition_animation_scale 0 || true
  adb_cmd shell settings put global animator_duration_scale 0 || true
  # A clean status bar makes the video look better.
  adb_cmd shell "settings put global sysui_demo_allowed 1" || true
  adb_cmd shell "am broadcast -a com.android.systemui.demo -e command enter" >/dev/null || true
  adb_cmd shell "am broadcast -a com.android.systemui.demo -e command clock -e hhmm 0900" \
    >/dev/null || true
  adb_cmd shell "am broadcast -a com.android.systemui.demo -e command battery -e level 100 \
    -e plugged false" >/dev/null || true
  adb_cmd shell "am broadcast -a com.android.systemui.demo -e command network -e wifi show \
    -e level 4" >/dev/null || true
}

restore_screen() {
  adb_cmd shell "am broadcast -a com.android.systemui.demo -e command exit" >/dev/null || true
  adb_cmd shell settings put global window_animation_scale 1 || true
  adb_cmd shell settings put global transition_animation_scale 1 || true
  adb_cmd shell settings put global animator_duration_scale 1 || true
}

# --- Mode maestro -----------------------------------------------------------

record_with_maestro() {
  log "Recording the flow with Maestro"
  mkdir -p "${OUTPUT_DIR}"
  # The option --local keeps the video on this machine. Without it, Maestro
  # sends the picture to its own servers.
  (cd "${PROJECT_ROOT}" && maestro record --local "${FLOW_FILE}") || return 2

  # Maestro writes the video into ~/.maestro/tests/<date>/.
  local newest
  newest="$(find "${HOME}/.maestro/tests" -name '*.mp4' -type f -print0 2>/dev/null \
    | xargs -0 ls -t 2>/dev/null | head -1 || true)"
  if [[ -n "${newest}" ]]; then
    cp "${newest}" "${OUTPUT_DIR}/fudge-demo.mp4"
    log "Video: ${OUTPUT_DIR}/fudge-demo.mp4"
  else
    warn "Maestro wrote no video. Look in ${HOME}/.maestro/tests."
  fi
}

# --- Mode adb ---------------------------------------------------------------

record_with_adb() {
  log "Recording the screen with the Android tools"
  mkdir -p "${OUTPUT_DIR}"
  local device_file="/sdcard/fudge-demo.mp4"

  adb_cmd shell "rm -f ${device_file}" || true
  adb_cmd shell screenrecord --bit-rate 8000000 --time-limit "${MAX_RECORD_SECONDS}" \
    "${device_file}" &
  local recorder_pid=$!
  sleep 2

  log "Running the test of the user interface"
  local test_result=0
  (cd "${FRONTEND_DIR}" && ./gradlew --console=plain connectedDebugAndroidTest) \
    || test_result=2

  # screenrecord writes the file only after a clean stop.
  adb_cmd shell "pkill -INT screenrecord" || true
  wait "${recorder_pid}" 2>/dev/null || true
  sleep 2

  adb_cmd pull "${device_file}" "${OUTPUT_DIR}/fudge-demo.mp4" >/dev/null
  log "Video: ${OUTPUT_DIR}/fudge-demo.mp4"
  return "${test_result}"
}

# --- Work after the recording -----------------------------------------------

make_gif() {
  local video="${OUTPUT_DIR}/fudge-demo.mp4"
  [[ -f "${video}" ]] || return 0
  has ffmpeg || { warn "ffmpeg is missing, so the script writes no GIF."; return 0; }

  log "Writing a GIF for the README"
  ffmpeg -y -loglevel error -i "${video}" \
    -vf "fps=12,scale=360:-1:flags=lanczos,split[a][b];[a]palettegen[p];[b][p]paletteuse" \
    "${OUTPUT_DIR}/fudge-demo.gif"
  log "GIF: ${OUTPUT_DIR}/fudge-demo.gif"
}

# --- Main -------------------------------------------------------------------

main() {
  check_tools
  build_and_install
  prepare_screen
  trap restore_screen EXIT

  local mode="${MODE}"
  if [[ "${mode}" == "auto" ]]; then
    if has maestro; then
      mode="maestro"
    else
      mode="adb"
      warn "Maestro is missing, so the script uses the Android tools.
Install Maestro for a video with the name of each step:
  curl -Ls https://get.maestro.mobile.dev | bash"
    fi
  fi

  log "Mode: ${mode}"
  case "${mode}" in
    maestro) record_with_maestro ;;
    adb) record_with_adb ;;
    *) fail "Unknown mode '${mode}'. Use maestro or adb." ;;
  esac

  make_gif
  log "Ready."
}

main "$@"
