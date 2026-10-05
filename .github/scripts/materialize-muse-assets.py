#!/usr/bin/env python3
"""Materialize Muse's repaired high-fidelity atlas into per-screen PNG assets.

The fixed atlas is a valid 5x3 WebP assembled from the approved Muse visual
references. Seven screens use the cleaned, app-ready designs (no phone frame or
irrelevant sample branding), while the remaining reference screens are
normalized edge-to-edge. Android loads the individual PNGs at runtime, so the
large-atlas decoder can never produce the corruption seen in device testing.
"""

from __future__ import annotations

import base64
import hashlib
import subprocess
import tempfile
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
OUTPUT_DIR = ROOT / "app" / "src" / "main" / "assets" / "muse_reference"
FIXED_DIR = ROOT / "app" / "src" / "main" / "assets" / "muse_reference_fixed"
CELL_W = 941
CELL_H = 1672
COLS = 5
ROWS = 3
EXPECTED_CHUNKS = 17
EXPECTED_SIZE = 2467504
EXPECTED_SHA256 = "48395d7897f852a69692c7d8f7e2e1f56f4a54e42aa8cd5083ec5ab48bc76158"

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


def read_fixed_atlas() -> bytes:
    chunks = sorted(FIXED_DIR.glob("muse_fixed_atlas.b64.*"))
    if len(chunks) != EXPECTED_CHUNKS:
        raise SystemExit(
            f"Expected {EXPECTED_CHUNKS} repaired atlas chunks, found {len(chunks)}"
        )
    encoded = "".join(p.read_text(encoding="ascii").strip() for p in chunks)
    decoded = base64.b64decode(encoded, validate=True)
    if len(decoded) != EXPECTED_SIZE:
        raise SystemExit(f"Repaired atlas size mismatch: {len(decoded)}")
    digest = hashlib.sha256(decoded).hexdigest()
    if digest != EXPECTED_SHA256:
        raise SystemExit(f"Repaired atlas hash mismatch: {digest}")
    return decoded


def main() -> None:
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    atlas_bytes = read_fixed_atlas()
    print("Verified repaired Muse atlas:", EXPECTED_SHA256, len(atlas_bytes))

    with tempfile.TemporaryDirectory(prefix="muse-fixed-atlas-") as tmp:
        tmp_dir = Path(tmp)
        atlas_webp = tmp_dir / "muse_reference_atlas_fixed.webp"
        atlas_png = tmp_dir / "muse_reference_atlas_fixed.png"
        atlas_webp.write_bytes(atlas_bytes)

        subprocess.run(
            ["dwebp", str(atlas_webp), "-quiet", "-o", str(atlas_png)],
            check=True,
        )

        with Image.open(atlas_png) as atlas:
            atlas.load()
            if atlas.size != (CELL_W * COLS, CELL_H * ROWS):
                raise SystemExit(f"Unexpected repaired atlas dimensions: {atlas.size}")
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
                output = OUTPUT_DIR / f"screen_{name}.png"
                cell.save(output, format="PNG", optimize=False, compress_level=3)
                print(
                    name,
                    output.stat().st_size,
                    hashlib.sha256(output.read_bytes()).hexdigest(),
                )


if __name__ == "__main__":
    main()
