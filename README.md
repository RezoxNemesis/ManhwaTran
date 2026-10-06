# Muse

Muse is a local-first Android music player being rebuilt from the former dormant ManhwaTran repository.

## Product direction

Muse is designed around a dark botanical visual system with real local playback, a Media3 background session, library discovery through Android MediaStore, queue controls, favourites, artist/album browsing, sleep tools, and capability-aware audio enhancement.

The product does **not** require an account or paid cloud service for core use.

## Current foundation

Implemented on the Muse conversion branch:

- canonical Muse package/application identity and exact approved Muse logo integration
- Jetpack Compose navigation and dark botanical design system
- local audio discovery through Android MediaStore
- user-selected audio import through Android's Storage Access Framework with persistent read access
- real Media3 ExoPlayer + MediaSessionService background playback
- notification/lock-screen/system media-session integration path
- play/pause/seek/next/previous, shuffle, repeat, queue reorder/remove, play-next, add-to-queue and queue-to-playlist
- playback-session restoration for queue, position, repeat and shuffle, including imported content URIs
- real local artwork loading with safe fallbacks
- persistent liked songs, listening history and editable user playlists
- Songs / Albums / Artists / Playlists library browsing and local search
- deterministic local trending, six Muse mood mixes and local Song Radio without a fabricated cloud catalogue
- local plain-text and synced LRC lyric import with active-line following and seek-by-line
- real capability-aware Android Equalizer, BassBoost, Virtualizer and LoudnessEnhancer attachment with A/B bypass
- persistent service-owned Sleep Timer with 10 / 30 / 60 / 90 minute presets
- real imported-media management on the Downloads screen
- actionable playback/error states and graceful unsupported-effect fallbacks
- GitHub Actions unit-test, lint and debug-build pipeline
- the authoritative 15-screen visual reference set is indexed in `design/MUSE_UI_REFERENCE_MANIFEST.md`

Core Muse functionality remains local-first and requires no paid cloud API or subscription.

## Build requirements

- JDK 17
- Android SDK 36
- Gradle 8.13
- Android Gradle Plugin 8.13.2
- Kotlin 2.2.20

The repository CI installs Gradle 8.13 directly while the Gradle wrapper is added in a later hardening step.

## Architecture

- Kotlin
- Jetpack Compose
- AndroidX Navigation
- Media3 ExoPlayer
- Media3 MediaSessionService
- MediaStore
- DataStore
- Coroutines / Flow

## Privacy

Muse is local-first. Library metadata and listening state should remain on-device unless the user explicitly invokes a future network feature. No advertising SDK or mandatory analytics SDK is required.

## Engineering status

This repository is under active conversion. Do not treat the current branch as release-ready until CI, runtime playback, foreground/background media controls, permissions, audio effects, persistence, and all 15 approved UI reference screens are verified on Android hardware/emulator.
