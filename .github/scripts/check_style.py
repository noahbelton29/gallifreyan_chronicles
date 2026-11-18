#!/usr/bin/env python3

import os
import re
import sys

ROOTS = ["common/src/main/java", "fabric/src/main/java", "neoforge/src/main/java"]

# per-addon loggers are named after the addon on purpose
LOGGER_ALLOWED = {"Constants.java", "ModAddonContext.java"}

CODE_MASK = re.compile(r'"(?:\\.|[^"\\\n])*"|//[^\n]*|/\*.*?\*/', re.S)
RULES = [
    (re.compile(r"^import [\w.]+\.\*;", re.M), "Wildcard import. Import each class by name."),
    (re.compile(r"^import org\.jetbrains\.annotations\.(Nullable|NotNull);", re.M),
     "JetBrains nullability annotation. Use org.jspecify.annotations.Nullable / NonNull."),
    (re.compile(r"(?<![\w.])(?:\d+\.\d*|\.\d+|\d+)(?:[eE][+-]?\d+)?F\b"), "Float literal with F. Use a lowercase f."),
]
LOGGER = re.compile(r"\b(LoggerFactory\.getLogger|LogUtils\.getLogger)\b")


def github(kind, message, path=None, line=None):
    if os.environ.get("GITHUB_ACTIONS") == "true":
        where = f" file={path},line={line}" if path else ""
        print(f"::{kind}{where}::{message}")
    else:
        print(f"{kind.upper()}: {path + ':' + str(line) + ': ' if path else ''}{message}")


def blank(match):
    return re.sub(r"[^\n]", " ", match.group(0))


def main():
    problems = 0
    for root in ROOTS:
        for dirpath, _, filenames in os.walk(root):
            for filename in filenames:
                if not filename.endswith(".java"):
                    continue
                path = os.path.join(dirpath, filename)
                text = open(path, encoding="utf-8").read()
                code = CODE_MASK.sub(blank, text)
                checks = list(RULES)
                if filename not in LOGGER_ALLOWED:
                    checks.append((LOGGER, "Own logger. Use Constants.LOG."))
                for pattern, message in checks:
                    for match in pattern.finditer(code):
                        github("error", message, path, code.count("\n", 0, match.start()) + 1)
                        problems += 1
    if problems:
        print(f"{problems} style problem(s).")
        sys.exit(1)
    print("No style problems.")


if __name__ == "__main__":
    main()
