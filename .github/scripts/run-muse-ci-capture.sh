#!/usr/bin/env bash
set -euo pipefail

# Run only the fast diagnostic pass on the Batch 2 branch.
# Keep one command in the Android emulator runner workflow script field.
if [[ "${GITHUB_REF_NAME:-}" == "feature/muse-batch2-live-atmosphere-previews" ]]; then
  export MUSE_CAPTURE_SMOKE_ONLY=1
fi

exec bash .github/scripts/capture-muse-ui.sh
