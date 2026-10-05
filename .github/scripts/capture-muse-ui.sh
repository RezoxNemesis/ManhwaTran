#!/usr/bin/env bash
set -euo pipefail

adb wait-for-device
until [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" == "1" ]]; do
  sleep 2
done
adb shell input keyevent KEYCODE_WAKEUP || true
adb shell wm dismiss-keyguard || true
sleep 5
adb shell settings put system font_scale 1.0
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Silence the Google SDK setup helper on the headless CI emulator.
adb shell pm disable-user --user 0 com.google.android.googlesdksetup || true
adb shell am force-stop com.google.android.googlesdksetup || true
adb shell settings put global device_provisioned 1 || true
adb shell settings put secure user_setup_complete 1 || true

adb shell pm grant com.rezoxnemesis.muse android.permission.READ_MEDIA_AUDIO || true

# The headless Pixel launcher can ANR under SwiftShader and obscure motion captures.
# Muse is started explicitly, so disable the launcher for this CI-only emulator.
adb shell am force-stop com.google.android.apps.nexuslauncher || true
adb shell pm disable-user --user 0 com.google.android.apps.nexuslauncher || true
adb shell input keyevent KEYCODE_BACK || true

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
  sleep 6

  if ! adb shell pidof com.rezoxnemesis.muse >/dev/null 2>&1; then
    echo "Muse process exited while opening route: $route" >&2
    adb logcat -d -v threadtime | tail -n 500 >&2 || true
    exit 1
  fi

  # Do not accept a splash screen as proof that a requested route rendered.
  # UiAutomator can briefly return a null/stale root on the headless emulator,
  # so retry the semantic check instead of failing on a single flaky dump.
  case "$route" in
    home) expected="Home" ;;
    explore) expected="Explore" ;;
    library) expected="Your Library" ;;
    equalizer) expected="Equalizer" ;;
    settings) expected="Settings" ;;
    sleep) expected="Sleep Timer" ;;
    *) expected="" ;;
  esac

  if [[ -n "$expected" ]]; then
    matched=0
    for attempt in 1 2 3 4 5; do
      rm -f /tmp/muse-window.xml
      adb shell uiautomator dump /sdcard/muse-window.xml >/dev/null 2>&1 || true
      adb pull /sdcard/muse-window.xml /tmp/muse-window.xml >/dev/null 2>&1 || true
      if [[ -s /tmp/muse-window.xml ]] && grep -Fq "$expected" /tmp/muse-window.xml; then
        matched=1
        break
      fi
      sleep 2
    done

    if [[ "$matched" -ne 1 ]]; then
      echo "Requested route '$route' did not reach expected UI '$expected' after retries" >&2
      cat /tmp/muse-window.xml >&2 || true
      exit 1
    fi
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

# Record a real interaction pass so the moving tab capsule, directional page
# transitions and widget motion are verified on an Android runtime, not only
# inferred from static screenshots.
adb shell settings put system font_scale 1.0
adb shell am force-stop com.rezoxnemesis.muse
adb shell am start -W   -n com.rezoxnemesis.muse/.MainActivity   --es com.rezoxnemesis.muse.extra.OPEN_ROUTE home
sleep 3
adb shell input keyevent KEYCODE_BACK || true
sleep 0.5

adb shell screenrecord --size 540x1140 --bit-rate 2500000 --time-limit 15 /sdcard/muse-motion-demo.mp4 &
record_pid=$!
sleep 1

# Pixel 4 emulator bottom destinations: Home, Explore, Library, Equalizer, Muse Lab.
adb shell input tap 320 2090
sleep 1
adb shell input tap 535 2090
sleep 1
adb shell input tap 750 2090
sleep 1
adb shell input tap 965 2090
sleep 1
adb shell input tap 320 2090
sleep 1

# Exercise the Explore segmented tabs so their shared moving capsule is captured.
adb shell input tap 110 470
sleep 0.6
adb shell input tap 320 470
sleep 0.6
adb shell input tap 520 470
sleep 0.6
adb shell input tap 735 470
sleep 0.6
adb shell input tap 930 470
sleep 0.8

# Move to Equalizer and exercise the direct-touch curve. These swipes must
# visibly track the finger without waiting for service round-trips.
adb shell input tap 750 2090
sleep 1
adb shell input swipe 245 760 245 1120 900
sleep 0.5
adb shell input swipe 245 980 835 650 1400
sleep 0.8

wait "$record_pid" || true
adb pull /sdcard/muse-motion-demo.mp4 ui-captures/muse-motion-demo.mp4
test -s ui-captures/muse-motion-demo.mp4

file ui-captures/*.png ui-captures/muse-motion-demo.mp4
sha256sum ui-captures/*.png ui-captures/muse-motion-demo.mp4
