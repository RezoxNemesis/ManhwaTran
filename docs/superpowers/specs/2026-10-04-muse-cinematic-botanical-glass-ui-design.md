# Muse Cinematic Botanical Glass UI Design

**Date:** 2026-10-04  
**Status:** Approved for implementation  
**Visual source of truth:** `design/MUSE_UI_REFERENCE_MANIFEST.md` and the 15 approved Muse UI references.

## Goal

Bring runtime Muse substantially closer to the approved 15-screen references without packaging the references as raster UI screenshots. The shipped UI must preserve real Compose controls, real library/playback state, Android accessibility and adaptive layouts while matching the references' visual intensity: near-black forest depth, realistic wet botanical richness, luminous green edges, layered glass, artwork contrast, and smooth high-confidence motion.

## Non-negotiable visual target

The reference screens are not merely "green UI." Their look comes from the interaction of several layers:

1. **Near-black depth base**
   - Backgrounds sit close to black rather than medium green.
   - Green is concentrated in light, glass edges, foliage and active state.
   - Large dark regions give highlights room to glow.

2. **Botanical depth**
   - Foreground leaves are large, cropped and high-detail.
   - Midground foliage is softer and darker.
   - Background bokeh/light blooms are diffuse and selective.
   - Leaves use multiple green values, glossy highlights and occasional bright droplets rather than flat vector ovals.
   - Botanical elements frame content instead of competing with text.

3. **Glass hierarchy**
   - Panels read as dark translucent green-black glass, not opaque green cards.
   - Borders are brighter than panel centres and can use two-stage rims: a subtle cool green outer line plus a brighter active highlight.
   - Glass has inner light, soft depth shadow and a faint reflective top/diagonal sheen.
   - Active elements glow. Inactive surfaces remain restrained.
   - Repeated cards must preserve contrast so the whole screen does not become uniformly luminous.

4. **Controlled luminous green**
   - Neon green is an accent, not the base fill.
   - Strongest glow is reserved for selected tabs, active transport, active EQ controls, selected timer, current progress and primary calls to action.
   - White text remains dominant for readability.

5. **Artwork contrast**
   - Album art stays colourful and crisp against the dark botanical shell.
   - Artwork containers use clean corner radii and subtle luminous edge treatment.
   - Artwork must never be recoloured green merely to fit the theme.

6. **Motion quality**
   - Navigation and sheets use short spring/depth transitions rather than weak fades.
   - Pressed glass compresses slightly and reduces highlight intensity before rebounding.
   - Mini-player and bottom sheets use confident translation/scale motion.
   - Motion must not interrupt playback, scrolling, or accessibility.
   - Reduced-motion users get a lower-motion path without losing state feedback.

## Architecture

### 1. `MuseAtmosphere`

Create a reusable full-screen atmospheric layer responsible for:
- near-black forest gradient foundation;
- multiple procedural botanical layers with visibly different depth;
- soft bokeh/glow blooms;
- slow, low-amplitude parallax where safe;
- optional screen-specific intensity profiles;
- foreground leaf framing that never blocks hit targets;
- no continuous expensive blur pass.

The 15 reference images remain design targets only. Runtime visuals are generated from native Compose drawing, gradients, vector/path foliage, optional lightweight approved botanical raster texture assets created specifically for the app, and real album art.

### 2. `MuseGlassSurface`

Replace ad-hoc card styling with one reusable glass primitive supporting:
- standard / strong / elevated / selected / destructive variants;
- layered translucent dark-green-black fill;
- inner highlight;
- luminous green rim;
- soft outer shadow;
- optional directional sheen;
- pressed/focused/selected motion states;
- stable readable contrast over any atmospheric background.

### 3. Glass controls

Build reusable variants for:
- primary CTA;
- compact icon action;
- segmented/filter chip;
- bottom navigation item;
- mini-player shell;
- bottom sheet/menu row;
- queue/list row;
- EQ control container;
- timer preset;
- search field.

All controls must continue to call real Muse functions.

### 4. Screen translation rules

#### Splash
Match the dramatic dark botanical composition and luminous logo emphasis. Do not render a fake loading bar. If startup is immediate, use a short deterministic transition.

#### Home
Preserve the reference hierarchy: greeting/logo area, search, four quick actions, recently played, strong mini-player and glass navigation. Use real tracks and real counts. Replace Premium with Muse Lab.

#### Explore
Use real local categories, moods, library-derived trends and Muse Flow entry points. Never fabricate streaming charts or online popularity.

#### Library
Use real Songs / Albums / Artists / Playlists tabs, recent/liked/imported collections and real counts.

#### Now Playing
Make artwork the visual anchor. Use strong glass transport controls, crisp progress, real quality metadata where available, favourites and real action navigation.

#### Lyrics
Use real imported/embedded lyrics. Synced emphasis may glow green. No fake lyrics when unavailable.

#### Queue
Maintain drag/reorder functionality. Use strong current-track distinction and glass bottom actions.

#### Playlists
Use real user playlists and favourites. No fabricated playlist catalogue.

#### Equalizer
Keep only capability-detected controls. Strengthen visual depth around EQ bands and active effects. Do not show unsupported effects as working toggles.

#### Settings
Group real settings into glass sections. Do not add cloud/account/premium settings that Muse does not support.

#### Artist
Use local artist information. Replace fictional follower/monthly-listener metrics with real local metrics such as local track count, album count, favourites, play count or Muse Flow actions.

#### Album
Use real album metadata and track list. Preserve strong artwork/header treatment and real Play / Shuffle / Favourite actions.

#### Downloads
Represent Muse-managed/imported/offline-accessible files truthfully. Do not imply protected network downloading.

#### Sleep Timer
Preserve the strong circular focus of the reference. Use the real timer states and presets already implemented.

#### More Options
Use a strong glass bottom-sheet treatment. Every row maps to a real action. Muse Flow replaces generic/fake Song Radio where appropriate.

## Feature truthfulness

The visual layer must never cause a capability claim Muse cannot support.

- Premium -> Muse Lab.
- Online trending -> local trends/listening intelligence.
- Streaming popularity/listeners/follow -> local artist insights and Muse Flow.
- Unsupported surround/3D audio -> hidden or capability-labeled, never fake.
- Download More -> import/add available local audio or other legitimate real flow already supported.
- Fake bitrate -> display actual bitrate only when metadata exists.
- Fake album/song artwork -> real artwork or intentional Muse placeholder.

## Performance rules

- No mandatory paid service or SDK.
- No always-on full-screen blur.
- Prefer static gradients, cached drawing and low-frequency motion.
- Atmospheric motion must suspend when app is backgrounded.
- Large lists retain lazy rendering.
- Glass effects must not introduce per-row bitmap blur.
- Target smooth 60 Hz interaction on normal supported devices where hardware allows.
- Provide reduced-motion behavior.
- Playback reliability outranks decoration.

## Accessibility

- Preserve Android semantics and TalkBack labels.
- Minimum practical touch targets remain 48dp.
- Text contrast must remain readable over the brightest foliage.
- Dynamic text must not be baked into artwork.
- Selected states cannot rely on glow alone.
- Reduced-motion users keep state changes without large translations.

## Verification

Each of the 15 runtime screens must be screenshot-reviewed against its matching reference for:
- composition;
- black/green balance;
- glass darkness and rim intensity;
- botanical depth;
- highlight/glow placement;
- spacing and hierarchy;
- artwork prominence;
- typography contrast;
- navigation geometry;
- correct replacement of mock-only features with real Muse functions.

Automated gates remain:
- unit tests;
- lint;
- instrumentation-test compilation;
- debug assembly;
- release assembly;
- APK verification.

## Success criteria

The pass succeeds when:
1. Muse still behaves as a real local-first music player.
2. No reference screenshot is used as a fake interactive screen.
3. The first impression is visually much closer to the 15 references than the current video.
4. Glass reads as strong layered green-black glass, not a weak transparent card.
5. Botanical depth is visibly richer than the current procedural oval-leaf background.
6. Active green glow is intense but controlled.
7. Core screens remain readable, responsive and functional.
8. No planned Beast feature is removed to obtain the visual result.
