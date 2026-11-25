#!/usr/bin/env bash
set -euo pipefail

APK="${1:?APK path required}"
PACKAGE=app.yomu
ACTIVITY="$PACKAGE/eu.kanade.tachiyomi.ui.main.MainActivity"
OUT=smoke-results
mkdir -p "$OUT"
test -f "$APK"

"$ANDROID_HOME/emulator/emulator" -avd yomu-ci -no-window -no-audio -no-snapshot \
  -gpu swiftshader_indirect -no-boot-anim > "$OUT/emulator.log" 2>&1 &
EMULATOR_PID=$!
cleanup() {
  adb logcat -d > "$OUT/logcat.txt" 2>&1 || true
  adb logcat -d -b crash > "$OUT/crashes.txt" 2>&1 || true
  adb shell dumpsys activity activities > "$OUT/activities.txt" 2>&1 || true
  adb emu kill >/dev/null 2>&1 || true
  kill "$EMULATOR_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

timeout 360 adb wait-for-device
booted=false
for attempt in $(seq 1 120); do
  if [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" = 1 ]; then
    booted=true
    break
  fi
  sleep 3
done
if [ "$booted" != true ]; then
  echo "::error::Android emulator did not complete boot"
  exit 1
fi

adb shell input keyevent 82
adb shell settings put system accelerometer_rotation 0
adb shell settings put global window_animation_scale 0
adb shell settings put global transition_animation_scale 0
adb shell settings put global animator_duration_scale 0
adb install -g "$APK"
adb logcat -c

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
