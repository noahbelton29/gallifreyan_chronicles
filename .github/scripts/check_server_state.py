#!/usr/bin/env python3

import os
import re
import sys

ROOTS = ["common/src/main/java", "fabric/src/main/java", "neoforge/src/main/java"]
ALLOWLIST = ".github/static-state-allowlist.txt"
FIELD = re.compile(
    r"static\s+final\s+[\w.]*(?:Map|Set|List|Deque|Queue)\s*<[^;=]*?\b([A-Za-z_]\w*)\s*=\s*([^;]+);", re.S)
IMMUTABLE = re.compile(r"^(ServerState\.track|ClientState\.track|List\.of|Set\.of|Map\.of|Map\.ofEntries|List\.copyOf|Set\.copyOf|"
                       r"Map\.copyOf|Collections\.(unmodifiable|empty|singleton)|Immutable)")


def github(kind, message, path=None, line=None):
    if os.environ.get("GITHUB_ACTIONS") == "true":
        where = f" file={path},line={line}" if path else ""
        print(f"::{kind}{where}::{message}")
    else:
        print(f"{kind.upper()}: {path + ':' + str(line) + ': ' if path else ''}{message}")


def load_allowlist():
    allowed = set()
    if os.path.exists(ALLOWLIST):
        for raw in open(ALLOWLIST, encoding="utf-8"):
            entry = raw.split("#", 1)[0].strip()
            if entry:
                allowed.add(entry)
    return allowed


def main():
    allowed = load_allowlist()
    found = {}
    for root in ROOTS:
        for dirpath, _, filenames in os.walk(root):
            for filename in filenames:
                if not filename.endswith(".java"):
                    continue
                path = os.path.join(dirpath, filename).replace("\\", "/")
                text = open(path, encoding="utf-8").read()
                for match in FIELD.finditer(text):
                    name, init = match.group(1), match.group(2).strip()
                    if IMMUTABLE.match(init):
                        continue
                    line = text.count("\n", 0, match.start()) + 1
                    client = "client" in path[len(root) + 1:].split("/")
                    found[f"{path}:{name}"] = (path, line, client)

    new = {key: where for key, where in found.items() if key not in allowed}
    for key, (path, line, client) in sorted(new.items()):
        name = key.rsplit(":", 1)[1]
        wrapper, when = ("ClientState", "the player disconnects") if client else ("ServerState", "the server stops")
        github("error", f"{name} is a static collection that isn't cleared when {when}, so it can carry over "
                        f"between worlds. Wrap it: {wrapper}.track(new ...). If it's meant to last for the whole game "
                        f"(a registry, or a cache of textures, models or other asset data), add \"{key}\" to "
                        f"{ALLOWLIST} with a reason.", path, line)
    for key in sorted(allowed - set(found)):
        github("warning", f"{key} is on {ALLOWLIST} but no longer needs to be. Remove it.")

    print(f"{len(found)} static collection(s) are not tracked: "
          f"{len(found) - len(new)} allowed, {len(new)} new.")
    return 1 if new else 0


if __name__ == "__main__":
    sys.exit(main())
