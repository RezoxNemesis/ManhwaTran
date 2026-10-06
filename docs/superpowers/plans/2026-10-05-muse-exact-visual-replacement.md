# Muse Exact Visual Replacement Plan

Date: 2026-10-05
Branch: feature/muse-foundation

## Problem

Runtime evidence in 1000066366.mp4 shows two UI systems rendering simultaneously:
- the approved botanical/glass reference art as a background, and
- the previous Compose screen hierarchy above it.

This creates duplicated headings, duplicated controls, mismatched spacing and a visibly layered/incorrect design.

## Non-negotiable outcome

The 15 approved screens become the visible UI surface. The legacy Compose layout must not render on those routes.

App size is not a constraint. Visual fidelity wins over binary size.

## Architecture

### 1. Exact visual surface
Use the approved 15 screens in this fixed order:
0 Splash
1 Home
2 Now Playing
3 Lyrics
4 Queue
5 Explore
6 Library
7 Playlists
8 Equalizer
9 Settings
10 Artist
11 Album
12 Downloads
13 Sleep Timer
14 More Options

Normalize the newer 864x1536 screens to 941x1672 using high-quality Lanczos resampling. Build one 5x3 lossless WebP atlas so every screen has identical geometry and no lossy recompression.

### 2. No legacy visible overlay
MuseApp routes to a new MuseExactVisualExperience instead of the former Scaffold + legacy screen composables.

The exact visual engine:
- draws only the approved screen image,
- has no visible legacy cards, text, headers, mini-player or bottom navigation,
- adds transparent accessibility-aware hit regions for real actions.

### 3. Real interactions preserved
Wire hotspots to existing Muse domain/playback operations:
- primary navigation,
- playback play/pause/previous/next/shuffle/repeat,
- favourites,
- queue clear/shuffle/save,
- album/artist play actions,
- downloads/import entry,
- sleep timer presets/start,
- settings navigation,
- More Options navigation.

No fake streaming or paid-service behavior is added.

### 4. Splash
Show the approved splash screen briefly on cold launch before Home.

### 5. Asset packaging
Keep the visual atlas directly inside app/src/main/assets/muse_reference as lossless base64 chunks, because GitHub's connected text API cannot commit raw binary contents. The decoded bytes are the lossless atlas packaged into the APK.

The manifest records cell dimensions, order and SHA-256.

### 6. Verification gates
Before publishing:
- exact 15-cell atlas geometry validation,
- SHA-256 manifest validation,
- unit tests,
- lint,
- instrumentation compilation,
- debug APK assembly,
- release assembly,
- runtime UI capture,
- verify legacy visible shell is absent,
- publish installable debug APK.

## Cleanup
Once the exact-screen engine is verified, the former MuseApp implementation remains unreachable and is removed/refactored so it cannot accidentally be reintroduced as a visible overlay.
