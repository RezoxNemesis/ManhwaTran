# Muse

Muse is a local-first Android music player being rebuilt from the former dormant ManhwaTran repository.

## Product direction

Muse is designed around a dark botanical visual system with real local playback, a Media3 background session, library discovery through Android MediaStore, queue controls, favourites, artist/album browsing, sleep tools, and capability-aware audio enhancement.

The product does **not** require an account or paid cloud service for core use.

## Current foundation

Implemented on the Muse conversion branch:

- Muse package/application identity
- Jetpack Compose application shell
- local audio discovery through MediaStore
- runtime audio-library permission flow
- Media3 ExoPlayer + MediaSessionService
- background/session controller bridge
- Home, Explore, Library, Now Playing, Queue, Lyrics empty state, Liked Songs, Playlists foundation, Artist, Album, Downloads empty state, Sleep Timer, Settings, More Options, Equalizer capability foundation, and Muse Lab navigation
- persistent liked-song state with DataStore
- real sleep timer that pauses playback
- dark botanical Muse design tokens
- GitHub Actions build/test/lint pipeline

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
