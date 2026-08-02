#!/usr/bin/env python3
"""Restore Java string/comment tokens changed by an earlier broad migration pass."""

from __future__ import annotations

import re
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_ROOT = ROOT / "src/main/java"
JAVA_NON_CODE = re.compile(
    r'("(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|//[^\n]*(?:\n|$)|/\*.*?\*/)',
    re.DOTALL,
)


def baseline(path: Path) -> str | None:
    relative = path.relative_to(ROOT).as_posix()
    result = subprocess.run(
        ["git", "show", f"HEAD:{relative}"],
        cwd=ROOT,
        text=True,
        encoding="utf-8",
        errors="replace",
        capture_output=True,
        check=False,
    )
    return result.stdout if result.returncode == 0 else None


def main() -> None:
    changed = 0
    skipped = 0
    for path in JAVA_ROOT.rglob("*.java"):
        original = baseline(path)
        if original is None:
            continue
        current = path.read_text(encoding="utf-8")
        old_tokens = list(JAVA_NON_CODE.finditer(original))
        current_tokens = list(JAVA_NON_CODE.finditer(current))
        if len(old_tokens) != len(current_tokens):
            skipped += 1
            continue
        repaired = current
        for old, new in zip(reversed(old_tokens), reversed(current_tokens)):
            repaired = repaired[:new.start()] + old.group(0) + repaired[new.end():]
        if repaired != current:
            path.write_text(repaired, encoding="utf-8", newline="\n")
            changed += 1
    print(f"Restored strings/comments in {changed} files; skipped {skipped}")


if __name__ == "__main__":
    main()
