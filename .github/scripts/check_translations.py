#!/usr/bin/env python3
import json
import os
import re
import sys

MOD_ID = "gallifreyan_chronicles"
LANG_DIR = f"common/src/main/resources/assets/{MOD_ID}/lang"
SOURCE_DIRS = ["common/src/main/java", "fabric/src/main/java", "neoforge/src/main/java"]
# Datagen defines keys rather than using them
SKIP_DIRS = ["datagen"]

KEY_LITERAL = re.compile(r'"([a-z0-9_]+\.' + MOD_ID + r'\.[a-z0-9_.]*[a-z0-9_])"(\s*\+)?')
STATE_CALL = re.compile(r'\bstate\(\s*"([a-z0-9_]+)"\s*,\s*"([a-z0-9_]+)"')
STATE_PREFIX = f"tooltip.{MOD_ID}.control_state."

# Literals that match the key shape but are not translation keys
NOT_KEYS_PREFIXES = ("mixins.", "fabric.", "neoforge.")


def github(kind, message, path=None, line=None):
    if os.environ.get("GITHUB_ACTIONS") == "true":
        where = f" file={path},line={line}" if path else ""
        print(f"::{kind}{where}::{message}")
    else:
        loc = f"{path}:{line}: " if path else ""
        print(f"{kind.upper()}: {loc}{message}")


def load_lang(name):
    path = os.path.join(LANG_DIR, name)
    with open(path, encoding="utf-8") as f:
        return json.load(f)


def find_used_keys():
    used = {}  # key -> first (path, line)
    for root_dir in SOURCE_DIRS:
        for dirpath, dirnames, filenames in os.walk(root_dir):
            if any(part in SKIP_DIRS for part in dirpath.replace("\\", "/").split("/")):
                continue
            for filename in filenames:
                if not filename.endswith(".java"):
                    continue
                path = os.path.join(dirpath, filename).replace("\\", "/")
                with open(path, encoding="utf-8") as f:
                    for number, line in enumerate(f, 1):
                        stripped = line.strip()
                        if stripped.startswith("//") or stripped.startswith("*") or stripped.startswith("/*"):
                            continue
                        for match in KEY_LITERAL.finditer(line):
                            key, concatenated = match.group(1), match.group(2)
                            if concatenated or key.startswith(NOT_KEYS_PREFIXES):
                                continue
                            used.setdefault(key, (path, number))
                        for match in STATE_CALL.finditer(line):
                            label, value = match.groups()
                            used.setdefault(f"{STATE_PREFIX}label.{label}", (path, number))
                            used.setdefault(f"{STATE_PREFIX}value.{value}", (path, number))
    return used


def main():
    english = load_lang("en_us.json")
    others = {name: load_lang(name) for name in sorted(os.listdir(LANG_DIR))
              if name.endswith(".json") and name != "en_us.json"}

    used = find_used_keys()
    missing_english = {k: v for k, v in used.items() if k not in english}
    for key, (path, line) in sorted(missing_english.items()):
        github("error", f"Translation key '{key}' is used here but missing from en_us.json", path, line)

    warnings = 0
    for name, lang in others.items():
        missing = sorted(k for k in english if k not in lang)
        for key in missing:
            github("warning", f"'{key}' is in en_us.json but missing from {name}")
        warnings += len(missing)
        extra = sorted(k for k in lang if k not in english)
        for key in extra:
            github("warning", f"'{key}' is in {name} but not in en_us.json")
        warnings += len(extra)

    print(f"Checked {len(used)} keys used in code against {len(english)} English translations "
          f"and {len(others)} other language(s): {len(missing_english)} missing, {warnings} warning(s)")
    return 1 if missing_english else 0


if __name__ == "__main__":
    sys.exit(main())
