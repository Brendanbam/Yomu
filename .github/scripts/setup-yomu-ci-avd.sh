#!/usr/bin/env bash
set -euo pipefail

test -e /dev/kvm || { echo "::error::KVM unavailable on this runner"; exit 1; }
sudo chmod 666 /dev/kvm
tools="$ANDROID_HOME/cmdline-tools/latest/bin"
image="system-images;android-35;google_apis;x86_64"
export ANDROID_USER_HOME="$RUNNER_TEMP/yomu-android"
export ANDROID_AVD_HOME="$ANDROID_USER_HOME/avd"
mkdir -p "$ANDROID_AVD_HOME"

"$tools/sdkmanager" --install "platform-tools" "emulator" "$image"
"$tools/avdmanager" create avd --force --name yomu-ci --package "$image" \
  --path "$ANDROID_AVD_HOME/yomu-ci.avd" <<< no
"$ANDROID_HOME/emulator/emulator" -list-avds | grep -Fx yomu-ci

printf 'ANDROID_USER_HOME=%s\nANDROID_AVD_HOME=%s\n' "$ANDROID_USER_HOME" "$ANDROID_AVD_HOME" >> "$GITHUB_ENV"
echo "$ANDROID_HOME/platform-tools" >> "$GITHUB_PATH"
