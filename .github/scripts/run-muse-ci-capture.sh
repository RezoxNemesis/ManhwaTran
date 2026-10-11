#!/usr/bin/env bash
set -euo pipefail

# The emulator GitHub action runs one shell command per script entry. Keep
# the workflow input to a single invocation; select the short smoke pass here.
if [[ "\${GITHUB_REF_NAME:-}" == "feature/muse-batch2-live-atmosphere-previews" ]]; then
  export MUSE_CAPTURE_SMOKE_ONLY=1
fi

exec bash .github/scripts/capture-muse-ui.sh
