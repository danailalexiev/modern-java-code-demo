#!/usr/bin/env bash
# Usage: scripts/reset.sh <tag>
#
# Checks out a talk tag and warms the compiler, so a demo act can start from a
# known-good, already-compiled state. Run this once before each act.
set -euo pipefail

if [[ $# -ne 1 ]]; then
    echo "usage: $0 <tag>" >&2
    exit 1
fi

tag="$1"
repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if ! git rev-parse -q --verify "refs/tags/${tag}" >/dev/null; then
    echo "error: no such tag: ${tag}" >&2
    echo "available tags:" >&2
    git tag --list 'step-*' | sed 's/^/  /' >&2
    exit 1
fi

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: working tree is not clean, refusing to check out ${tag}" >&2
    git status --short >&2
    exit 1
fi

git checkout --quiet "$tag"
./gradlew -q compileJava
echo "on ${tag}, compiled clean."
