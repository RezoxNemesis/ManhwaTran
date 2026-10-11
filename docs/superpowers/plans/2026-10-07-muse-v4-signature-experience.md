# Muse V4 Signature Experience Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build Muse 0.4.0 as a richer, more distinctive and more stable six-world experience with true world morphing, interaction memory and adaptive visual performance while preserving V3's successful visual identity.

**Architecture:** Add pure, testable V4 state/model components first, then integrate them into the Compose renderer behind the existing `MuseVisualProfile` system. Keep playback/audio independent from visual state. Use a single V4 experience source of truth plus bounded interaction/memory state and a local performance governor, then harden runtime capture so visual motion is release evidence rather than a screenshot-only check.

**Tech Stack:** Kotlin, Jetpack Compose, AndroidX, Media3, JUnit 4, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-10-07-muse-v4-signature-experience-design.md`

## Global Constraints

- Base V4 exactly on V3 head `232a98e4f80e180559945f96eed3f42bc7604e5b`.
- Preserve all six V3 world identities as visual regression targets.
- Never use full-screen screenshots or still images as UI.
- No paid services, cloud rendering dependency, subscription dependency or external runtime requirement.
- Playback/audio processing remains isolated from visual experimentation.
- New visual state must be bounded; no unbounded particle/memory lists.
- No uncontrolled per-frame allocations in hot draw paths where avoidable.
- Release target is `versionCode = 4`, `versionName = "0.4.0"`.
- Behaviour changes follow RED -> GREEN TDD.

## Review Focus

1. Rapid world switching while a morph is active must retarget from current visual progress without queued stale transitions. Covered in Task 2 tests.
2. Long playback sessions with repeated transients must not grow Music Memory without bound. Covered in Task 3 tests.
3. Performance quality must not oscillate around thresholds or disable hero effects. Covered in Task 4 tests.
4. Non-Verdant worlds must not inherit generic rainforest droplet behaviour. Covered in Tasks 1 and 5 tests.
5. App background/inactive rendering must reduce expendable visual work without affecting playback state. Covered in Tasks 4 and 5 tests.

---

### Task 1: Central WorldExperience model and water-language policy

**Files:**
- Create: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseWorldExperience.kt`
- Modify: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseLivingWorlds.kt`
- Test: `app/src/test/java/com/rezoxnemesis/muse/ui/MuseWorldExperienceTest.kt`

**Interfaces:**
- Consumes: `MuseVisualProfile`, existing V3 living-world style, light, scene and chrome functions.
- Produces: `MuseWorldExperience`, `MuseWaterLanguage`, `MuseVisualProfile.worldExperience()`.

- [ ] **Step 1: Write failing tests** proving all six profiles have unique experience signatures and water languages match the spec: Verdant=full physical droplets, Ocean=refractive/tidal, Aurora=condensation, Moon=haze-condensation, Rose=restrained dew, Ember=dry.
- [ ] **Step 2: Run `:app:testDebugUnitTest --tests '*MuseWorldExperienceTest*'` and verify RED** because V4 types/functions do not exist.
- [ ] **Step 3: Implement the minimal model/mappings**, reusing V3 style/light/geometry/chrome data rather than duplicating them.
- [ ] **Step 4: Run the focused test and full `:app:testDebugUnitTest`; verify GREEN.**
- [ ] **Step 5: Commit** `feat: define Muse V4 world experiences`.

### Task 2: Interruption-safe WorldMorph state engine

**Files:**
- Create: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseWorldMorph.kt`
- Test: `app/src/test/java/com/rezoxnemesis/muse/ui/MuseWorldMorphTest.kt`

**Interfaces:**
- Consumes: `MuseWorldExperience` from Task 1.
- Produces: `MuseWorldMorphState`, `MuseWorldMorphSnapshot`, `retargetMuseWorldMorph(...)`, `sampleMuseWorldMorph(...)`.

- [ ] **Step 1: Write failing tests** for start/finish interpolation, rapid retargeting from the current sampled state, clamped progress and no stale queued target.
- [ ] **Step 2: Run focused tests and verify RED** because morph API is absent.
- [ ] **Step 3: Implement a pure deterministic morph reducer/sampler** with bounded state and no Compose dependency.
- [ ] **Step 4: Run focused + full unit suite; verify GREEN.**
- [ ] **Step 5: Commit** `feat: add interruption-safe world morph engine`.

### Task 3: Bounded World Echo, Music Memory, Living Focus and Breathing state

**Files:**
- Create: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseWorldInteraction.kt`
- Test: `app/src/test/java/com/rezoxnemesis/muse/ui/MuseWorldInteractionTest.kt`

**Interfaces:**
- Consumes: `MuseWorldExperience`, `MuseAudioSpectrum`.
- Produces: bounded `MuseWorldEcho`, `MuseMusicMemoryState`, `MuseLivingFocusState`, `MuseWorldBreathingState`, gesture-momentum sample helpers.

- [ ] **Step 1: Write failing tests** for six distinct echo languages, memory accumulation/decay/cap, quiet-vs-playing breathing, focus decay and clamped gesture momentum.
- [ ] **Step 2: Run focused tests and verify RED.**
- [ ] **Step 3: Implement pure bounded state functions** with deterministic decay and hard caps.
- [ ] **Step 4: Run focused + full unit suite; verify GREEN.**
- [ ] **Step 5: Commit** `feat: add living world interaction memory`.

### Task 4: Local Visual Performance Governor

**Files:**
- Create: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseVisualPerformanceGovernor.kt`
- Test: `app/src/test/java/com/rezoxnemesis/muse/ui/MuseVisualPerformanceGovernorTest.kt`

**Interfaces:**
- Consumes: frame-duration/headroom samples and foreground/activity state.
- Produces: `MuseVisualQualityBudget` including secondary particle/bokeh/detail multipliers while hero features remain enabled.

- [ ] **Step 1: Write failing tests** for hysteresis, gradual degrade/recover, inactive/background reduction and protected hero flags.
- [ ] **Step 2: Run focused tests and verify RED.**
- [ ] **Step 3: Implement minimal governor** with bounded rolling pressure, separate degrade/recovery thresholds and no rapid quality flapping.
- [ ] **Step 4: Run focused + full unit suite; verify GREEN.**
- [ ] **Step 5: Commit** `feat: add adaptive Muse visual performance governor`.

### Task 5: Compose V4 integration and shared visual clock

**Files:**
- Create: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseSignatureExperienceV4.kt`
- Modify: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseApp.kt`
- Modify: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseBotanicalBackdrop.kt`
- Modify: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseLivingWorldsV3.kt`
- Modify: `app/src/main/java/com/rezoxnemesis/muse/ui/MuseAtmosphereV2.kt`
- Test: `app/src/test/java/com/rezoxnemesis/muse/ui/MuseV4RenderPolicyTest.kt`

**Interfaces:**
- Consumes: Tasks 1-4 public V4 APIs plus existing V3 render functions.
- Produces: `MuseSignatureExperienceV4` composable integration, shared visual phase, profile-aware water policy, morph/interaction render parameters and lifecycle-aware quality application.

- [ ] **Step 1: Write failing render-policy tests** proving Ember suppresses physical droplets, Ocean uses refractive water, inactive state reduces secondary density while hero identity remains enabled, and each profile maps to a distinct interaction/render policy.
- [ ] **Step 2: Run focused tests and verify RED.**
- [ ] **Step 3: Implement V4 integration** so world/background/chrome/navigation consume one morph progress and one coordinated visual clock; route V2 water through the Task 1 water policy.
- [ ] **Step 4: Add world-specific rendering for Echo/Memory/Focus/Breathing** using bounded state from Task 3 and budget multipliers from Task 4.
- [ ] **Step 5: Run focused + full unit suite, lint and debug assembly; verify GREEN.**
- [ ] **Step 6: Commit** `feat: integrate Muse V4 signature experience`.

### Task 6: Runtime capture hardening and motion proof

**Files:**
- Modify: `.github/workflows/muse-ui-screenshot.yml`
- Modify: `.github/scripts/capture-muse-ui.sh`

**Interfaces:**
- Consumes: running V4 APK and Muse Lab controls.
- Produces: six settled world screenshots plus dedicated V4 world-transition/motion MP4 capture and clearer failure diagnostics.

- [ ] **Step 1: Add script-level assertions/checks** that distinguish confirmed app process exit from emulator/ADB transport failure and require expected V4 capture outputs.
- [ ] **Step 2: Run shell syntax/static checks in CI and verify failures if expected outputs/diagnostics are absent.**
- [ ] **Step 3: Add representative world-switch recording sequence** covering Verdant->Aurora->Ember->Moon->Ocean->Rose and preserve existing route/equalizer captures.
- [ ] **Step 4: Run runtime workflow and inspect generated captures; do not call V4 complete if worlds regress toward colour-only sameness.**
- [ ] **Step 5: Commit** `test: harden Muse V4 runtime visual proof`.

### Task 7: Release version, final verification and visual acceptance

**Files:**
- Modify: `app/build.gradle.kts`
- Modify documentation/PR description only if required by verified implementation.

**Interfaces:**
- Consumes: completed Tasks 1-6.
- Produces: Muse 0.4.0 APK artifacts and verified draft PR state.

- [ ] **Step 1: Set `versionCode = 4` and `versionName = "0.4.0"`.**
- [ ] **Step 2: Run complete Android CI: unit tests, lint, instrumentation compile, debug/release assemble, APK verification.**
- [ ] **Step 3: Inspect six runtime screenshots and transition recording against the V3 protected baseline.**
- [ ] **Step 4: Run whole-branch review against this plan/spec; fix Important/Critical findings with RED->GREEN tests.**
- [ ] **Step 5: Keep PR draft until runtime visual acceptance is green; commit release metadata** `chore: prepare Muse 0.4.0 preview`.
