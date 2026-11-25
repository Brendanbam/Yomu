#!/usr/bin/env bash
set -euo pipefail

APK="${1:?APK path required}"
UPGRADE_FROM="${2:-}"
PACKAGE=app.yomu
ACTIVITY="$PACKAGE/eu.kanade.tachiyomi.ui.main.MainActivity"
OUT=smoke-results
mkdir -p "$OUT"
test -f "$APK"

adb() {
  timeout 60 "$ANDROID_HOME/platform-tools/adb" "$@"
}

adb start-server
"$ANDROID_HOME/emulator/emulator" -avd yomu-ci -no-window -no-audio -no-snapshot \
  -gpu swangle -no-boot-anim > "$OUT/emulator.log" 2>&1 &
EMULATOR_PID=$!
cleanup() {
  if ! kill -0 "$EMULATOR_PID" 2>/dev/null; then
    tail -80 "$OUT/emulator.log"
    return
  fi
  adb logcat -d > "$OUT/logcat.txt" 2>&1 || true
  adb logcat -d -b crash > "$OUT/crashes.txt" 2>&1 || true
  adb shell dumpsys activity activities > "$OUT/activities.txt" 2>&1 || true
  adb emu kill >/dev/null 2>&1 || true
  kill "$EMULATOR_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

booted=false
boot_deadline=$((SECONDS + 360))
while (( SECONDS < boot_deadline )); do
  if ! kill -0 "$EMULATOR_PID" 2>/dev/null; then
    echo "::error::Android emulator exited before boot"
    tail -80 "$OUT/emulator.log"
    exit 1
  fi
  if [ "$(timeout 10 "$ANDROID_HOME/platform-tools/adb" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = 1 ]; then
    booted=true
    break
  fi
  sleep 3
done
if [ "$booted" != true ]; then
  echo "::error::Android emulator did not complete boot"
  adb devices -l
  tail -80 "$OUT/emulator.log"
  exit 1
fi

adb shell input keyevent 82
adb shell settings put system accelerometer_rotation 0
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0

assert_running() {
  adb shell pidof "$PACKAGE" >/dev/null
  adb shell dumpsys activity activities > "$OUT/current-activity.txt"
  grep -E 'mResumedActivity|topResumedActivity|ResumedActivity' "$OUT/current-activity.txt" | grep -F "$PACKAGE/"
  adb logcat -d -b crash > "$OUT/crashes.txt"
  if grep -Fq "Process: $PACKAGE," "$OUT/crashes.txt"; then
    echo "::error::Yomu crashed during emulator smoke test"
    exit 1
  fi
}

launch() {
  adb shell am start -W -n "$ACTIVITY" | tee "$OUT/launch.txt"
  grep -Fq "Status: ok" "$OUT/launch.txt"
  sleep 8
  assert_running
}

adb shell wm size 1080x1920
adb shell wm density 420

if [ -n "$UPGRADE_FROM" ]; then
  adb install -g "$UPGRADE_FROM"
  adb logcat -c
  launch
  first_install_time="$(adb shell dumpsys package "$PACKAGE" | tr -d '\r' | sed -n 's/^[[:space:]]*firstInstallTime=//p')"
  test -n "$first_install_time"
  adb install -r -g "$APK"
  launch
  updated_install_time="$(adb shell dumpsys package "$PACKAGE" | tr -d '\r' | sed -n 's/^[[:space:]]*firstInstallTime=//p')"
  test "$first_install_time" = "$updated_install_time"
  adb exec-out screencap -p > "$OUT/01-signed-upgrade.png"
  echo "Signed upgrade installed in place and launched."
  exit 0
fi

adb install -g "$APK"
adb logcat -c
launch
adb exec-out screencap -p > "$OUT/01-phone-launch.png"

adb shell wm size 1600x1000
adb shell wm density 160
sleep 5
assert_running
adb exec-out screencap -p > "$OUT/02-expanded-window.png"

adb shell am force-stop "$PACKAGE"
launch
adb exec-out screencap -p > "$OUT/03-expanded-restart.png"

python3 .github/scripts/yomu-library-smoke.py
assert_running

adb shell wm size reset
adb shell wm density reset
echo "Launch, backup restore, reader launch, tablet controls, and restart checks passed."

