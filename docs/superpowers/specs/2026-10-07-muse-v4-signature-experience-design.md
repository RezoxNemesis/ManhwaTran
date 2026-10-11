# Muse V4 Signature Experience Design

**Date:** 2026-10-07  
**Base:** V3 Living Worlds head `232a98e4f80e180559945f96eed3f42bc7604e5b`  
**Target release:** Muse 0.4.0, versionCode 4  
**Branch:** `feature/muse-signature-experience-v4`

## 1. Goal

Muse V4 turns the successful V3 Living Worlds direction into a more detailed, more distinctive, smoother and more stable signature experience. V4 must preserve the visual moments that already feel exceptional while deepening the sense that each of the six worlds is a physical environment rather than a recoloured theme.

The release must remain a real native Android/Jetpack Compose application. Full-screen screenshots or still-image UI substitutes are forbidden. Static images may be used only as decorative/media assets.

## 2. Product principles

1. Preserve the V3 "wow" moments as regression targets.
2. Each world must remain recognisable by geometry, material, lighting and motion even with colour removed.
3. UI chrome and atmosphere must behave like one material system.
4. World changes must morph, not merely fade or recolour.
5. Rich visual behaviour must not compromise music playback stability or normal navigation.
6. Visual complexity must scale locally to device headroom without visibly collapsing the hero experience.
7. No paid services, cloud rendering dependency, subscription dependency or external runtime requirement.

## 3. Six protected world identities

### Verdant Rain
Wet organic broadleaf geometry, realistic dew, attached water, coalescence, sliding/falling droplets, rain interaction, soft biological specular response and weighted foliage sway.

### Aurora Glass
Iridescent crystalline/faceted geometry, travelling refracted highlights, prismatic ribbons, low-density airy particles and sharper, cleaner motion.

### Midnight Ember
Charred copper/shard geometry, lower-key lighting, heat staining, warm internal glow, ember lift, subtle fracture language and short punchy physical motion.

### Moonlit Violet
Silver-violet lance geometry, broad upper illumination, slow suspended haze, lunar haloing, restrained motion and calm long-duration breathing.

### Ocean Pulse
Tidal frond geometry, refractive lens behaviour, moving caustics, pressure-depth feeling, light shafts, interference/ripple motion and elastic navigation physics.

### Rose Noir
Velvet/petal geometry, deep wine shadows, soft edge illumination, petal orbit/bloom behaviour and softer compress/rebound motion.

These identities must not converge into one shared botanical scene with different colours.

## 4. WorldExperience architecture

V4 introduces a central `WorldExperience` definition for each visual profile. It becomes the source of truth for:

- material micro-detail
- foliage/foreground geometry
- light field
- atmospheric density
- world motion personality
- UI chrome material
- bottom-navigation physics
- idle/breathing behaviour
- interaction response
- music-memory response
- transition/morph signature
- performance-cost hints

Existing V3 data types should be reused or migrated where sensible rather than duplicated.

The interface shell remains recognisably Muse across Home, Explore, Library, Equalizer, Muse Lab and Settings. The world-specific experience alters material and behaviour, not basic information architecture.

## 5. WorldMorph transition engine

V3 arrival overlays become a true transition system that understands both the outgoing and incoming world.

A transition owns one shared progress signal and drives:

- background lighting
- foreground geometry
- atmosphere density
- UI glass/chrome
- Muse mark treatment
- bottom navigation material/physics
- particles
- environment-specific transition effects

Examples of intended morph language:

- Verdant -> Aurora: moisture contracts into reflective facets and ribbons.
- Aurora -> Ember: prism structures darken, fracture and warm into charred copper.
- Ember -> Moon: embers cool and disperse into slow lunar haze.
- Moon -> Ocean: haze stretches into refraction, caustics and tidal shafts.
- Ocean -> Rose: refractive forms fold into softer petal geometry.
- Rose -> Verdant: velvet/petals deepen into organic leaf structure and dew.

Transitions must be interruption-safe. If the user changes worlds again before a morph completes, the current visual state becomes the next transition's starting point instead of queuing or snapping through stale states.

## 6. Experimental interaction layer

### World Echo
Touch, swipe and scrub gestures leave short-lived world-specific physical consequences.

- Verdant: foliage deflection and travelling droplets.
- Aurora: fading refracted gesture ribbons.
- Ember: directional heated particles and glow traces.
- Moon: displaced mist.
- Ocean: directional ripple/pressure trails.
- Rose: petal displacement with slow return.

### Music Memory
Important audio events leave temporary visual state rather than disappearing instantly.

- Verdant: accumulated water/coalescence/release.
- Aurora: lingering refractive streaks/facets.
- Ember: glowing cracks/heat residue that cools.
- Moon: haze blooms that dissipate slowly.
- Ocean: interference rings that persist and overlap.
- Rose: bloom/petal memory that decays softly.

Memory state must be bounded and decay deterministically so it cannot accumulate indefinitely.

### Living Focus
The currently interacted UI region becomes the world's brightest/most physically emphasised object using the selected world's material language. Focus must remain subtle and must not reduce readability.

### World Breathing
During quiet music, paused playback or idle interaction, the environment enters a world-specific low-energy state instead of freezing.

### Gesture Momentum
Navigation and page transitions inherit a small amount of the user's input direction and speed so the bottom capsule, page content, light field and particles feel physically connected.

## 7. UI material detailing

All major glass/chrome surfaces continue to use one semantic Muse component system, but visual behaviour changes per world.

- Verdant: edge condensation, dew, wet specular travel.
- Aurora: internal facet lines, spectral edge refraction.
- Ember: forged darkness, heat staining, lower-edge glow, fracture detail.
- Moon: broad soft rims, silver diffusion, internal mist.
- Ocean: curved lensing, travelling caustics, pressure distortion.
- Rose: velvet falloff, petal-like highlights, rich soft shadows.

Typography remains consistent across worlds. World identity may modify surrounding light, glow and reflection, but not replace fonts or impair legibility.

## 8. Shared animation clock

V4 must not simply stack more independent infinite transitions.

Where practical, expensive atmospheric systems should derive from one or a small number of coordinated world clocks. This reduces redundant recomposition and makes cross-layer synchronisation possible.

The audio pipeline remains independent from the visual clock.

## 9. Visual Performance Governor

A local, offline `MuseVisualPerformanceGovernor` protects frame pacing while preserving the hero look.

The governor may adapt:

- secondary bokeh count
- distant particle density
- off-screen micro-detail
- secondary shimmer passes
- nonessential atmospheric sampling/detail

The governor must not remove:

- selected world identity
- primary lighting composition
- hero foreground geometry
- current touch response
- active transition/morph
- music-reactive hero behaviour
- essential UI chrome

Quality changes should be gradual and hysteretic rather than rapidly oscillating.

The governor must stop or reduce expendable work when the app is backgrounded or when expensive layers are not visible.

## 10. Stability contract

V4 must explicitly handle:

- rapid repeated world changes
- rapid primary-tab navigation
- playback start/stop/pause transitions
- app foreground/background lifecycle changes
- configuration/activity recreation where supported
- empty music library
- no active audio
- prolonged animation sessions
- switching between Muse Lab and other routes while transitions are active
- low visual headroom without breaking playback

Visual-engine failure must never interrupt audio playback.

## 11. Water-language correction

V2 leaf-bound water currently appears across profiles through shared wetness values. V4 must prevent generic wet botanical behaviour from leaking into all worlds.

Target behaviour:

- Verdant: full bead/coalescence/sliding/falling water physics.
- Ocean: water/refractive droplets may exist, but should read as tidal/lens behaviour rather than rainforest dew.
- Aurora: condensation/refraction only where appropriate.
- Moon: haze/soft condensation, no dominant botanical droplets.
- Rose: very restrained dew, subordinate to velvet/petal identity.
- Ember: effectively suppress wet droplet behaviour.

## 12. Audio response

Continue using real PCM spectrum information already available in V3.

The environment should distinguish:

- bass
- mid
- high
- transient
- overall energy

World-specific response mappings should avoid every world reacting to the same frequency in the same way.

## 13. Performance and lifecycle boundaries

- Playback/audio processing stays isolated from visual experimentation.
- Heavy world rendering must not run unnecessarily when the app is not visible.
- Short-lived interaction state must be bounded.
- Transition state must be cancellation-safe.
- No unbounded particle lists.
- No uncontrolled per-frame allocations in hot drawing paths where avoidable.
- Prefer deterministic seeded geometry for reproducible testing/capture.

## 14. Testing strategy

Use test-driven development for behaviour changes.

Required automated coverage includes:

1. six unique `WorldExperience` definitions
2. distinct morph signatures for all six worlds
3. interruption-safe morph state
4. bounded Music Memory decay
5. world-specific World Echo response
6. performance governor hysteresis and protected hero features
7. water-language policy per world
8. lifecycle pause/resume behaviour for expensive visuals
9. existing audio spectrum behaviour remains intact
10. existing navigation/equalizer/playback tests remain green

Android CI must pass unit tests, lint, instrumentation compilation, debug assembly and release assembly.

## 15. Runtime visual proof

The runtime capture workflow must be hardened before it is considered release evidence.

It must capture:

- all six worlds after settling
- a dedicated world-switch motion recording
- at least several representative morph pairs
- primary navigation motion
- Equalizer interaction
- Muse Lab world selection
- playback start/stop visual response

The capture script must fail fast if the app process exits, but emulator/ADB failure must be distinguishable from a confirmed app crash.

## 16. Release boundary

V4 release target:

- `versionCode = 4`
- `versionName = "0.4.0"`

V4 work happens on a branch based exactly on the known-good V3 head `232a98e4f80e180559945f96eed3f42bc7604e5b`.

V3 remains preserved as the visual safety baseline. V4 is not complete merely because CI is green. It is complete only after both code verification and runtime visual review show that the six protected world identities remain distinct and the new experience is smoother and more stable.

## 17. Acceptance criteria

V4 is acceptable when:

- all six worlds remain instantly distinguishable beyond hue
- switching worlds feels like a coherent physical morph
- touch and music leave world-specific short-lived consequences
- idle/quiet behaviour feels alive rather than frozen
- UI materials visibly belong to the current world
- normal navigation and playback remain responsive
- rapid repeated switching does not produce stale queued transitions
- visual workload adapts without obvious quality popping
- no full-screen static UI shortcuts are introduced
- automated tests/builds are green
- runtime captures prove the actual motion and transition behaviour
