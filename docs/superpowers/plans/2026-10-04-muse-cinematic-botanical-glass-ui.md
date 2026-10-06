# Muse Cinematic Botanical Glass UI Implementation Plan

> **For agentic workers:** Execute task-by-task with fresh verification before completion claims.

**Goal:** Rebuild Muse's shared visual foundation so the real Compose UI approaches the approved 15-screen references while preserving existing functionality.

**Architecture:** Introduce a reusable native atmosphere renderer and reusable glass surface system. Migrate existing shared UI primitives first, then translate individual screens in reference order. No reference screenshot becomes a fake interactive screen.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, Android Media3 existing stack, native Canvas/Brush/Path/graphicsLayer animation APIs.

**Spec:** `docs/superpowers/specs/2026-10-04-muse-cinematic-botanical-glass-ui-design.md`

## Global Constraints

- Keep the 15 screenshots as visual targets, not packaged raster UI screens.
- Preserve existing playback, library, Muse Flow and audio functionality.
- No mandatory paid tools, services or SDKs.
- No fake controls or fake metadata.
- No continuous expensive full-screen blur.
- Maintain accessibility, Android insets and 48dp practical touch targets.
- Playback reliability outranks decoration.

## Tasks

### Task 1: Shared visual foundation
- Create `app/src/main/java/com/rezoxnemesis/muse/ui/MuseVisualSystem.kt`.
- Implement `MuseAtmosphere` with near-black depth, layered native botanical paths, selective bokeh/light blooms and dew highlights.
- Implement `MuseGlassSurface` and visual variants with dark green-black gradient fill, luminous rim, inner sheen and depth shadow.
- Add reusable `MuseGlassAction` for strong spring press feedback.
- Expand theme tokens in `MuseTheme.kt`.
- Replace the current root `BotanicalBackdrop` and shared `GlassCard` implementation.

### Task 2: Navigation and mini-player
- Upgrade bottom navigation shell to layered glass.
- Replace Premium-equivalent visual treatment with the existing Muse Lab destination.
- Upgrade mini-player with artwork emphasis, glass border, progress accent and press feedback.
- Preserve MediaSession-backed actions.

### Task 3: Home and Explore
- Translate reference hierarchy while keeping real data.
- Strengthen greeting/search/quick actions/recent sections.
- Use local trends and Muse Flow rather than fabricated online charts.
- Preserve empty states.

### Task 4: Library, playlists and queue
- Translate tabs, collection rows, queue current/up-next distinction and playlist hierarchy.
- Preserve reorder, save, shuffle and playlist operations.

### Task 5: Now Playing and Lyrics
- Increase artwork prominence and transport-control depth.
- Add stronger progress/active-state glow.
- Style real lyrics with synced emphasis where available.
- Keep missing lyrics honest.

### Task 6: Equalizer and audio surfaces
- Match reference intensity around EQ bands/effects.
- Display only runtime-supported effects.
- Add selected/active glow without making unsupported controls appear functional.

### Task 7: Album and Artist
- Translate large-art/header layouts.
- Replace fictional follower/listener metrics with local track/album/play/favourite statistics and Muse Flow actions.

### Task 8: Downloads, Sleep Timer, Settings, More Options
- Translate all remaining reference surfaces.
- Keep downloads/import wording truthful.
- Make Sleep Timer the strong circular focal design.
- Convert More Options into a strong glass sheet with only real actions.

### Task 9: Motion pass
- Add spring/depth navigation and sheet transitions.
- Add press compression/rebound to key glass controls.
- Add mini-player expansion motion where practical.
- Provide reduced-motion path.

### Task 10: Verification and visual closure
- Run unit tests, lint, instrumentation-test compilation, debug assembly and release assembly.
- Compare all 15 screens against their references for black/green balance, glass intensity, botanical depth, spacing and feature truthfulness.
- Fix regressions before declaring completion.
