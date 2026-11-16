#!/usr/bin/env python3
import re
import subprocess
import sys

SECTIONS = [("feat", "Added"), ("fix", "Fixed"), ("change", "Changed"), ("other", "Other")]
TYPE_TO_SECTION = {"feat": "feat", "fix": "fix", "refactor": "change", "perf": "change", "style": "change"}
SKIPPED_TYPES = {"ci", "chore", "build"}
COMMIT = re.compile(r"^(?P<type>[a-zA-Z]+)(?:\((?P<scope>[^)]*)\))?(?P<breaking>!)?:\s*(?P<text>.+)$")


def git(*args):
    return subprocess.run(["git", *args], check=True, capture_output=True, text=True).stdout.strip()


def previous_tag(tag):
    try:
        return git("describe", "--tags", "--abbrev=0", f"{tag}^")
    except subprocess.CalledProcessError:
        return None


def main():
    tag = sys.argv[1]
    previous = previous_tag(tag)
    commit_range = f"{previous}..{tag}" if previous else tag
    log = git("log", "--no-merges", "--format=%h%x00%s", commit_range)

    sections = {key: [] for key, _ in SECTIONS}
    breaking = []
    for line in log.splitlines():
        if not line:
            continue
        sha, subject = line.split("\x00", 1)
        match = COMMIT.match(subject)
        if match:
            kind = match.group("type").lower()
            if kind in SKIPPED_TYPES:
                continue
            text = match.group("text").strip()
            scope = match.group("scope")
            if scope:
                text = f"**{scope}:** {text}"
            section = TYPE_TO_SECTION.get(kind, "other")
            if match.group("breaking"):
                breaking.append(f"- {text} ({sha})")
        else:
            text, section = subject.strip(), "other"
        text = text[0].upper() + text[1:] if text else text
        sections[section].append(f"- {text} ({sha})")

    out = []
    if breaking:
        out += ["### ⚠️ Breaking changes", *breaking, ""]
    for key, title in SECTIONS:
        if sections[key]:
            out += [f"### {title}", *sections[key], ""]
    if not out:
        out = ["No notable changes.", ""]
    if previous:
        out.append(f"**Full changes:** `{previous}...{tag}`")
    print("\n".join(out).rstrip())


if __name__ == "__main__":
    main()
