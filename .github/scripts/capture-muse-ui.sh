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

ensure_adb_transport() {
  if ! timeout 12s adb get-state 2>/dev/null | grep -Fq "device"; then
    echo "Android emulator/ADB transport became unavailable; this is not evidence of a Muse app crash." >&2
    exit 2
  fi
}

ensure_muse_process() {
  ensure_adb_transport
  if ! timeout 12s adb shell pidof com.rezoxnemesis.muse >/dev/null 2>&1; then
    echo "Muse process exited while the Android transport is still healthy." >&2
    adb logcat -d -v threadtime | tail -n 500 >&2 || true
    exit 1
  fi
}

dump_ui_to() {
  local output="$1"

  for attempt in 1 2 3 4 5; do
    rm -f "$output"
    adb shell rm -f /sdcard/muse-window.xml >/dev/null 2>&1 || true
    adb shell uiautomator dump /sdcard/muse-window.xml >/dev/null 2>&1 || true
    adb pull /sdcard/muse-window.xml "$output" >/dev/null 2>&1 || true
    if [[ -s "$output" ]]; then
      return 0
    fi
    sleep 1
  done

  echo "UiAutomator did not produce a window dump: $output" >&2
  adb logcat -d -v threadtime | tail -n 250 >&2 || true
  return 1
}

tap_text_from_dump() {
  local dump_file="$1"
  local needle="$2"
  local tap_x
  local tap_y

  read tap_x tap_y < <(python3 - "$dump_file" "$needle" <<'PY'
import re
import sys
import xml.etree.ElementTree as ET

path, needle = sys.argv[1], sys.argv[2]
root = ET.parse(path).getroot()
for node in root.iter("node"):
    text = " ".join(
        part for part in (
            node.attrib.get("text") or "",
            node.attrib.get("content-desc") or "",
        )
        if part
    )
    if needle not in text:
        continue
    match = re.match(
        r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]",
        node.attrib.get("bounds", ""),
    )
    if match:
        x1, y1, x2, y2 = map(int, match.groups())
        print((x1 + x2) // 2, (y1 + y2) // 2)
        break
else:
    raise SystemExit(f"Could not find UI text: {needle}")
PY
  )

  adb shell input tap "$tap_x" "$tap_y"
}

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

  if ! timeout 12s adb get-state 2>/dev/null | grep -Fq "device"; then
    echo "Android emulator/ADB transport failed while opening route: $route" >&2
    exit 2
  fi
  if ! timeout 12s adb shell pidof com.rezoxnemesis.muse >/dev/null 2>&1; then
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
    tools) expected="Muse Lab" ;;
    settings) expected="Settings" ;;
    sleep) expected="Sleep Timer" ;;
    *) expected="" ;;
  esac

  if [[ -n "$expected" ]]; then
    matched=0
    for attempt in 1 2 3 4 5; do
      rm -f /tmp/muse-window.xml
      dump_ui_to /tmp/muse-window.xml || true
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

# Regression for the real-device bug reproduced in the user's video:
# starting a track from Library must reveal the mini player without allowing the
# bottom bar to measure to the full screen and push "Your Library" off-screen.
adb shell settings put system font_scale 1.0
adb shell am force-stop com.rezoxnemesis.muse
adb shell am start -W \
  -n com.rezoxnemesis.muse/.MainActivity \
  --es com.rezoxnemesis.muse.extra.OPEN_ROUTE library
sleep 6
dump_ui_to /tmp/muse-library-before-play.xml

python3 - <<'PY'
import re
import xml.etree.ElementTree as ET
path = "/tmp/muse-library-before-play.xml"
root = ET.parse(path).getroot()
target = None
for node in root.iter("node"):
    text = (node.attrib.get("text") or "") + " " + (node.attrib.get("content-desc") or "")
    if "Muse_Runtime" in text or "Muse Runtime" in text:
        target = node
        break
if target is None:
    raise SystemExit("Runtime demo track not found in Library UI")
m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", target.attrib.get("bounds",""))
if not m:
    raise SystemExit("Runtime demo track bounds unavailable")
x1,y1,x2,y2 = map(int,m.groups())
print(f"{(x1+x2)//2} {(y1+y2)//2}")
PY
read track_x track_y < <(python3 - <<'PY'
import re
import xml.etree.ElementTree as ET
root = ET.parse("/tmp/muse-library-before-play.xml").getroot()
for node in root.iter("node"):
    text = (node.attrib.get("text") or "") + " " + (node.attrib.get("content-desc") or "")
    if "Muse_Runtime" in text or "Muse Runtime" in text:
        m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", node.attrib.get("bounds",""))
        if m:
            x1,y1,x2,y2 = map(int,m.groups())
            print((x1+x2)//2, (y1+y2)//2)
            break
else:
    raise SystemExit(1)
PY
)
adb shell input tap "$track_x" "$track_y"
sleep 3
dump_ui_to /tmp/muse-library-after-play.xml
if ! grep -Fq "Your Library" /tmp/muse-library-after-play.xml; then
  echo "Starting playback expanded the mini player/bottom bar over the Library screen" >&2
  cat /tmp/muse-library-after-play.xml >&2 || true
  exit 1
fi
adb exec-out screencap -p > ui-captures/muse-library-playing-runtime.png
test -s ui-captures/muse-library-playing-runtime.png

capture_route equalizer muse-equalizer-runtime.png

# The Equalizer must remain present even on the CI emulator where the OEM
# Equalizer/Spatializer stack is absent. Muse's software DSP is the fallback.
dump_ui_to /tmp/muse-equalizer.xml
if ! grep -Fq "Muse Spatial 3D" /tmp/muse-equalizer.xml; then
  echo "Muse Spatial 3D compatibility control is missing" >&2
  cat /tmp/muse-equalizer.xml >&2 || true
  exit 1
fi
if grep -Fq "Equalizer unavailable" /tmp/muse-equalizer.xml; then
  echo "Software Equalizer fallback was not exposed" >&2
  cat /tmp/muse-equalizer.xml >&2 || true
  exit 1
fi

capture_route tools muse-lab-runtime.png
dump_ui_to /tmp/muse-lab.xml
if ! grep -Fq "Atmosphere Studio" /tmp/muse-lab.xml; then
  echo "Muse Lab did not expose the live Atmosphere Studio" >&2
  cat /tmp/muse-lab.xml >&2 || true
  exit 1
fi
if ! grep -Fq "Auto" /tmp/muse-lab.xml || ! grep -Fq "Verdant Rain" /tmp/muse-lab.xml; then
  echo "Muse Lab visual scene controls were not reachable" >&2
  cat /tmp/muse-lab.xml >&2 || true
  exit 1
fi

# Prove the scene cards are real controls, not decorative labels. Select a
# non-default profile through the running UI and verify the debug app persists it.
tap_text_from_dump /tmp/muse-lab.xml "Aurora Glass"
sleep 1
visual_prefs="$(adb shell run-as com.rezoxnemesis.muse   cat shared_prefs/muse_visual_preferences.xml 2>/dev/null | tr -d '\r' || true)"
if ! grep -Fq "aurora_glass" <<<"$visual_prefs"; then
  echo "Aurora Glass selection did not persist from the real Muse Lab control" >&2
  printf '%s\n' "$visual_prefs" >&2
  exit 1
fi
dump_ui_to /tmp/muse-lab-aurora.xml
tap_text_from_dump /tmp/muse-lab-aurora.xml "Downpour"
sleep 1
visual_prefs="$(adb shell run-as com.rezoxnemesis.muse   cat shared_prefs/muse_visual_preferences.xml 2>/dev/null | tr -d '\r' || true)"
if ! grep -Fq "downpour" <<<"$visual_prefs"; then
  echo "Downpour rain selection did not persist from the real Muse Lab control" >&2
  printf '%s\n' "$visual_prefs" >&2
  exit 1
fi
adb exec-out screencap -p > ui-captures/muse-lab-aurora-downpour-runtime.png
test -s ui-captures/muse-lab-aurora-downpour-runtime.png

# V3 visual acceptance requires every living world to be inspected independently.
# Select each profile through the real Compose controls, scrolling Muse Lab only
# when the target card is below the current viewport.
capture_living_world() {
  local label="$1"
  local slug="$2"
  local dump="/tmp/muse-world-${slug}.xml"
  local found=0

  adb shell am force-stop com.rezoxnemesis.muse
  adb shell am start -W \
    -n com.rezoxnemesis.muse/.MainActivity \
    --es com.rezoxnemesis.muse.extra.OPEN_ROUTE tools
  sleep 4

  for attempt in 1 2 3 4; do
    dump_ui_to "$dump"
    if grep -Fq "$label" "$dump"; then
      found=1
      break
    fi
    adb shell input swipe 540 1660 540 720 550
    sleep 1
  done

  if [[ "$found" -ne 1 ]]; then
    echo "Living World control not reachable: $label" >&2
    cat "$dump" >&2 || true
    exit 1
  fi

  tap_text_from_dump "$dump" "$label"
  sleep 3

  visual_prefs="$(adb shell run-as com.rezoxnemesis.muse \
    cat shared_prefs/muse_visual_preferences.xml 2>/dev/null | tr -d '\r' || true)"
  if ! grep -Fq "$slug" <<<"$visual_prefs"; then
    echo "Living World selection did not persist: $label / $slug" >&2
    printf '%s\n' "$visual_prefs" >&2
    exit 1
  fi

  adb exec-out screencap -p > "ui-captures/muse-world-${slug}-runtime.png"
  test -s "ui-captures/muse-world-${slug}-runtime.png"
}

capture_living_world "Verdant Rain" "verdant_rain"
capture_living_world "Aurora Glass" "aurora_glass"
capture_living_world "Midnight Ember" "midnight_ember"
capture_living_world "Moonlit Violet" "moonlit_violet"
capture_living_world "Ocean Pulse" "ocean_pulse"
capture_living_world "Rose Noir" "rose_noir"

# V4 acceptance requires proof of the actual world-to-world morphs, not only
# screenshots captured after each transition has already settled.
select_world_in_live_lab() {
  local label="$1"
  local slug="$2"
  local dump="/tmp/muse-live-${slug}.xml"
  local found=0

  ensure_muse_process
  for attempt in 1 2 3 4 5; do
    dump_ui_to "$dump"
    if grep -Fq "$label" "$dump"; then
      found=1
      break
    fi
    adb shell input swipe 540 1660 540 720 500
    sleep 0.7
  done

  if [[ "$found" -ne 1 ]]; then
    echo "V4 live morph control not reachable: $label" >&2
    cat "$dump" >&2 || true
    exit 1
  fi

  tap_text_from_dump "$dump" "$label"
  sleep 1.35
  ensure_muse_process

  visual_prefs="$(adb shell run-as com.rezoxnemesis.muse \
    cat shared_prefs/muse_visual_preferences.xml 2>/dev/null | tr -d '\r' || true)"
  if ! grep -Fq "$slug" <<<"$visual_prefs"; then
    echo "V4 live morph selection did not persist: $label / $slug" >&2
    printf '%s\n' "$visual_prefs" >&2
    exit 1
  fi
}

adb shell settings put system font_scale 1.0
adb shell am force-stop com.rezoxnemesis.muse
adb shell am start -W \
  -n com.rezoxnemesis.muse/.MainActivity \
  --es com.rezoxnemesis.muse.extra.OPEN_ROUTE tools
sleep 4
select_world_in_live_lab "Verdant Rain" "verdant_rain"

adb shell screenrecord --size 540x1140 --bit-rate 3000000 --time-limit 20 \
  /sdcard/muse-world-morph-v4.mp4 &
morph_record_pid=$!
sleep 0.8

select_world_in_live_lab "Aurora Glass" "aurora_glass"
select_world_in_live_lab "Midnight Ember" "midnight_ember"
select_world_in_live_lab "Moonlit Violet" "moonlit_violet"
select_world_in_live_lab "Ocean Pulse" "ocean_pulse"
select_world_in_live_lab "Rose Noir" "rose_noir"

wait "$morph_record_pid" || true
ensure_adb_transport
adb pull /sdcard/muse-world-morph-v4.mp4 ui-captures/muse-world-morph-v4.mp4
test -s ui-captures/muse-world-morph-v4.mp4

capture_route settings muse-settings-runtime.png
capture_route sleep muse-sleep-runtime.png
capture_route settings muse-settings-font130-runtime.png 1.3

# Regression: Now Playing must never strand the user on a decorative/blank
# surface. Android Back must return to the Home route even when Now Playing was
# opened directly.
adb shell settings put system font_scale 1.0
adb shell am force-stop com.rezoxnemesis.muse
adb shell am start -W   -n com.rezoxnemesis.muse/.MainActivity   --es com.rezoxnemesis.muse.extra.OPEN_ROUTE nowPlaying
sleep 4
adb shell input keyevent KEYCODE_BACK
sleep 2
adb shell uiautomator dump /sdcard/muse-window.xml >/dev/null 2>&1 || true
adb pull /sdcard/muse-window.xml /tmp/muse-back-window.xml >/dev/null 2>&1 || true
if ! grep -Fq "Home" /tmp/muse-back-window.xml; then
  echo "Now Playing Back did not return to Home" >&2
  cat /tmp/muse-back-window.xml >&2 || true
  exit 1
fi

# Regression for the real-device behaviour reported by the user: after leaving a
# deep-linked Now Playing screen, a normal launcher start must not resurrect the
# stale route or leave only the botanical background.
adb shell am force-stop com.rezoxnemesis.muse
adb shell am start -W   -a android.intent.action.MAIN   -c android.intent.category.LAUNCHER   -n com.rezoxnemesis.muse/.MainActivity
sleep 4
adb shell uiautomator dump /sdcard/muse-window.xml >/dev/null 2>&1 || true
adb pull /sdcard/muse-window.xml /tmp/muse-relaunch-window.xml >/dev/null 2>&1 || true
if ! grep -Fq "Home" /tmp/muse-relaunch-window.xml; then
  echo "Launcher relaunch did not land on Home" >&2
  cat /tmp/muse-relaunch-window.xml >&2 || true
  exit 1
fi

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

file ui-captures/*.png ui-captures/muse-motion-demo.mp4 ui-captures/muse-world-morph-v4.mp4
sha256sum ui-captures/*.png ui-captures/muse-motion-demo.mp4 ui-captures/muse-world-morph-v4.mp4
