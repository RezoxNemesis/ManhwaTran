#!/usr/bin/env python3
"""Materialize Muse's lossless atlas into stable per-screen PNG assets.

Android BitmapRegionDecoder showed device-dependent corruption on the bottom
edge of large WebP regions. Build-time materialization avoids that decoder path
entirely and also removes the fake status/navigation chrome baked into the
original design mockups while preserving the botanical pixels at full quality.
"""

from __future__ import annotations

import base64
import glob
import hashlib
import io
import os
import subprocess
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSET_DIR = ROOT / "app" / "src" / "main" / "assets" / "muse_reference"
CELL_W = 941
CELL_H = 1672
COLS = 5
ROWS = 3
TOP_TRIM = 48
BOTTOM_TRIM = 48

SCREENS = [
    "splash",
    "home",
    "now_playing",
    "lyrics",
    "queue",
    "explore",
    "library",
    "playlists",
    "equalizer",
    "settings",
    "artist",
    "album",
    "downloads",
    "sleep_timer",
    "more_options",
]


def read_atlas_bytes() -> bytes:
    chunks = sorted(ASSET_DIR.glob("muse_reference_atlas_lossless.b64.*"))
    if len(chunks) != 28:
        raise SystemExit(f"Expected 28 atlas chunks, found {len(chunks)}")
    encoded = "".join(p.read_text(encoding="ascii").strip() for p in chunks)
    return base64.b64decode(encoded, validate=False)


def normalize_cell(cell: Image.Image) -> Image.Image:
    # Mockup-only status/navigation chrome is not part of Muse itself.
    # Trim it before stretching back to the canonical 941x1672 surface.
    trimmed = cell.crop((0, TOP_TRIM, CELL_W, CELL_H - BOTTOM_TRIM))
    return trimmed.resize((CELL_W, CELL_H), Image.Resampling.LANCZOS)


def main() -> None:
    ASSET_DIR.mkdir(parents=True, exist_ok=True)
    atlas_bytes = read_atlas_bytes()
    print("Atlas sha256:", hashlib.sha256(atlas_bytes).hexdigest())

    # libwebp's dwebp is materially more reliable for this very large,
    # lossless atlas than Pillow's WebPAnimDecoder. Decode once to a temporary
    # PNG, then let Pillow do only deterministic crop/resize work.
    with tempfile.TemporaryDirectory(prefix="muse-atlas-") as tmp:
        tmp_dir = Path(tmp)
        atlas_webp = tmp_dir / "muse_reference_atlas.webp"
        atlas_png = tmp_dir / "muse_reference_atlas.png"
        atlas_webp.write_bytes(atlas_bytes)

        subprocess.run(
            [
                "dwebp",
                str(atlas_webp),
                "-quiet",
                "-o",
                str(atlas_png),
            ],
            check=True,
        )

        with Image.open(atlas_png) as atlas:
            atlas.load()
            if atlas.size != (CELL_W * COLS, CELL_H * ROWS):
                raise SystemExit(f"Unexpected atlas dimensions: {atlas.size}")
            atlas = atlas.convert("RGB")

            for index, name in enumerate(SCREENS):
                col = index % COLS
                row = index // COLS
                cell = atlas.crop(
                    (
                        col * CELL_W,
                        row * CELL_H,
                        (col + 1) * CELL_W,
                        (row + 1) * CELL_H,
                    )
                )
                normalized = normalize_cell(cell)
                output = ASSET_DIR / f"screen_{name}.png"
                normalized.save(output, format="PNG", optimize=False, compress_level=3)
                print(
                    name,
                    output.stat().st_size,
                    hashlib.sha256(output.read_bytes()).hexdigest(),
                )


if __name__ == "__main__":
    main()
