#!/usr/bin/env bash
set -euo pipefail

adb wait-for-device
adb shell settings put system font_scale 1.0
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Silence the Google SDK setup helper on the headless CI emulator.
adb shell pm disable-user --user 0 com.google.android.googlesdksetup || true
adb shell am force-stop com.google.android.googlesdksetup || true
adb shell settings put global device_provisioned 1 || true
adb shell settings put secure user_setup_complete 1 || true

adb shell pm grant com.rezoxnemesis.muse android.permission.READ_MEDIA_AUDIO || true

adb shell mkdir -p /sdcard/Music
adb push Muse_Runtime_Demo.wav /sdcard/Music/Muse_Runtime_Demo.wav
adb shell am broadcast \
  -a android.intent.action.MEDIA_SCANNER_SCAN_FILE \
  -d file:///sdcard/Music/Muse_Runtime_Demo.wav || true

mkdir -p ui-captures

capture_route() {
  local route="$1"
  local output="$2"
  local font_scale="${3:-1.0}"

  adb shell settings put system font_scale "$font_scale"
  adb shell am force-stop com.rezoxnemesis.muse
  adb shell am start -W \
    -n com.rezoxnemesis.muse/.MainActivity \
    --es com.rezoxnemesis.muse.extra.OPEN_ROUTE "$route"
  sleep 3

  if ! adb shell pidof com.rezoxnemesis.muse >/dev/null 2>&1; then
    echo "Muse process exited while opening route: $route" >&2
    adb logcat -d -v threadtime | tail -n 500 >&2 || true
    exit 1
  fi

  adb exec-out screencap -p > "ui-captures/$output"
  test -s "ui-captures/$output"
}

capture_route home muse-home-runtime.png
capture_route explore muse-explore-runtime.png
capture_route library muse-library-runtime.png
capture_route equalizer muse-equalizer-runtime.png
capture_route settings muse-settings-runtime.png
capture_route sleep muse-sleep-runtime.png
capture_route settings muse-settings-font130-runtime.png 1.3

file ui-captures/*.png
sha256sum ui-captures/*.png
