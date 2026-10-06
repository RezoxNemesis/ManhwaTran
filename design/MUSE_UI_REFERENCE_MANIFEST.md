# Muse 15-Screen Visual Reference Manifest

This manifest locks the exact high-quality UI references approved for Muse. The original high-quality source images remain visual references only. Runtime Muse must recreate the approved hierarchy, atmosphere and interaction quality with native Compose components backed by real user/library state. Full-screen mockups, flattened screenshots, invisible hotspot interfaces and reference images containing readable UI are forbidden as runtime surfaces. Decorative imagery may be used only for genuine artwork, texture or atmosphere that does not substitute for live controls.

The canonical identity is deep forest green / near-black, realistic wet botanical atmosphere, translucent green glass surfaces, controlled luminous-green accents, white high-contrast typography, rounded geometry and album-art contrast. Example mock content in the images is not production data.

| # | Screen | Canonical source dimensions | SHA-256 |
|---:|---|---|---|
| 01 | Splash | 941×1672 | `6e4d050efaa9649e368c8d98dbd4d9bb66186a5ba580f77fb336db26bbd03dbb` |
| 02 | Home | 941×1672 | `0f9088942bbca2fa258f2d59ad9b102d6b3e38d107a9581972bc929c1084d82a` |
| 03 | Now Playing | 941×1672 | `cf43291ce132154b6bd16911e8fabab4c8d611465390fb9bf5ce7bb4a596774a` |
| 04 | Lyrics | 941×1672 | `673fd33671cacaf1f8c2073f2de4a9776c5077881781a27c6f324a61e242245b` |
| 05 | Play Queue | 941×1672 | `affa1db2a583f52a1cb7668ea0a23853ff5b7cd8ddfdab1667892d58b0156621` |
| 06 | Explore | 941×1672 | `02108c9ef91729dc95558974f910737e69673b6f01075417f2f416860e901cd9` |
| 07 | Library | 941×1672 | `84d9083af4eab7de822bd574e245aebb75d9f28b6871177c154e98a1b82e678d` |
| 08 | Playlists | 941×1672 | `bfadf34cde4e82e26a6c15fab72281d7760a922e8d529a2b8ca2ebb5ba0735b3` |
| 09 | Equalizer | 941×1672 | `9616790c7a3f4fcbe4e97e13e6e2187018329a3462738d82d2394e0f029fc1b9` |
| 10 | Settings | 941×1672 | `7c4a52b9ff0548d922282eae444b130269cdd0298c5583df364acb8639b36d39` |
| 11 | Artist | 941×1672 | `6b1fc1044fa5fde5fad136c3c8fa2ce54a507150d5af633a7ab4972625719312` |
| 12 | Album | 941×1672 | `9fb5c0e1f045a50136869ce309aa802eea0491c0092963c126f15a57e9aee941` |
| 13 | Downloads | 941×1672 | `8342b0dc6baba6ea8604a903bc3de01303eb6fe80d4f7e13817627d82020a77d` |
| 14 | Sleep Timer | 941×1672 | `d1274e6ab64b3c16e3f9f3dc92e5fbe938e8204f9a0311dc02d8d9e5b18d7aaa` |
| 15 | More Options | 941×1672 | `1d2c6c8ce8a92166e267cf0b0987fb81e241412b022d7b602d095202c19d0c87` |

## Implementation rules

- Treat the 15 references as visual, interaction and feature-density contracts.
- Recreate the wet-leaf, rain, bokeh, glass and artwork character with live native layers. Decorative photographic assets are allowed only when they contain no baked UI and remain non-interactive atmosphere behind real Compose controls.
- Never let readable mock text or mock controls become the functional interface. Real Compose controls must sit above the artwork and own all interaction/state.
- Do not remove a visible reference action merely because it is inconvenient to implement. Implement a real useful behaviour when lawful/platform-feasible; otherwise show a truthful capability-disabled state rather than silently deleting it.
- Use the exact approved Muse leaf/music-note logo where branding appears.
- Replace example song titles, listener counts, bitrate labels and other mock data with real local state or intentional empty states.
- Keep primary navigation geometry close to the references while replacing the unapproved Premium concept with Muse Lab / real tools.
- Preserve Android accessibility, dynamic type, insets, touch targets and responsive layout even when the reference is visually denser.
- Text readability is a hard gate: white/high-contrast primary text, deliberate muted secondary text, ellipsis or bounded wrapping for long metadata, no overlapping labels, no clipped headings, and no reference artwork strong enough to swallow live typography.
- Do not add expensive full-screen blur or continuous animation that damages scrolling, battery life or playback reliability.
- Screenshot-review each implemented screen against the corresponding numbered reference before release.
