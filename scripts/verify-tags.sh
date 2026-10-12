#!/usr/bin/env bash
# Checks out every talk tag and runs `./gradlew check` against it, then runs
# the three negative checks from the prep checklist: each one applies a patch
# under scripts/negative/, confirms the build FAILS the way it's supposed to,
# and reverts the patch. Prints a PASS/FAIL table and exits non-zero if
# anything is wrong.
set -uo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

TAGS=(
    step-0-baseline
    step-1-sealed
    step-2-domain
    step-3-result
    step-4-tx-bug
    step-4-tx
    step-5-agent
)

if [[ -n "$(git status --porcelain)" ]]; then
    echo "error: working tree is not clean, refusing to run (checking out tags would lose it)" >&2
    git status --short >&2
    exit 1
fi

original_ref="$(git symbolic-ref --quiet --short HEAD || git rev-parse HEAD)"
log="$(mktemp)"

# The patch files themselves only exist as of step-5-agent. Copy them out
# before checking out any (possibly earlier) tag, so they stay readable no
# matter which tag's working tree is currently checked out.
patch_dir="$(mktemp -d)"
cp scripts/negative/*.patch "$patch_dir"/

cleanup() {
    git checkout --quiet -- . 2>/dev/null || true
    git checkout --quiet "$original_ref" 2>/dev/null || true
    rm -f "$log"
    rm -rf "$patch_dir"
}
trap cleanup EXIT

declare -a RESULTS=()
record() { RESULTS+=("$1|$2"); }

run_positive_check() {
    local tag="$1"
    git checkout --quiet "$tag"
    if ./gradlew -q check >"$log" 2>&1; then
        record "${tag}: ./gradlew check" "PASS"
    else
        record "${tag}: ./gradlew check" "FAIL"
        echo "--- ${tag}: ./gradlew check output ---" >&2
        tail -n 40 "$log" >&2
    fi
}

# A negative check is supposed to make the build FAIL; that failure is this
# check's PASS. If the build unexpectedly succeeds, the guardrail it's
# testing isn't working, and this check FAILs.
run_negative_check() {
    local label="$1" tag="$2" patch="$3" gradle_task="$4"
    git checkout --quiet "$tag"

    if ! git apply --check "$patch" 2>"$log"; then
        record "$label" "FAIL (patch did not apply)"
        cat "$log" >&2
        return
    fi
    git apply "$patch"

    if ./gradlew -q "$gradle_task" >"$log" 2>&1; then
        record "$label" "FAIL (build unexpectedly passed)"
        echo "--- ${label}: expected a failure, got none ---" >&2
    else
        record "$label" "PASS"
    fi

    git apply -R "$patch"
    git checkout --quiet -- .
}

for tag in "${TAGS[@]}"; do
    run_positive_check "$tag"
done

run_negative_check \
    "step-1-sealed (a): new sealed subtype -> non-exhaustive switch" \
    step-1-sealed "$patch_dir/a-non-exhaustive-switch.patch" compileJava

run_negative_check \
    "step-5-agent (b): default -> on controller switch -> guardrail fails" \
    step-5-agent "$patch_dir/b-default-on-guardrail.patch" check

run_negative_check \
    "step-5-agent (c): business exception in service -> ArchUnit fails" \
    step-5-agent "$patch_dir/c-business-exception-in-service.patch" check

echo
width=72
printf '%-*s %s\n' "$width" "CHECK" "RESULT"
printf '%-*s %s\n' "$width" "$(printf '%*s' "$width" '' | tr ' ' '-')" "------"

overall=0
for entry in "${RESULTS[@]}"; do
    label="${entry%%|*}"
    status="${entry#*|}"
    printf '%-*s %s\n' "$width" "$label" "$status"
    [[ "$status" == "PASS" ]] || overall=1
done

exit $overall
