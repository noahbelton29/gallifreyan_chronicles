#!/usr/bin/env python3

import os
import re
import sys

ROOTS = ["common/src/main/java", "fabric/src/main/java", "neoforge/src/main/java"]
ALLOWLIST = ".github/client-code-allowlist.txt"
CLIENT_REF = re.compile(r"\b(net\.minecraft\.client\.|com\.mojang\.blaze3d\.|uk\.nbforge\.gallifreyan_chronicles\.client\.)")


def github(kind, message, path=None, line=None):
    if os.environ.get("GITHUB_ACTIONS") == "true":
        where = f" file={path},line={line}" if path else ""
        print(f"::{kind}{where}::{message}")
    else:
        print(f"{kind.upper()}: {path + ':' + str(line) + ': ' if path else ''}{message}")


def is_client(path):
    return "client" in path.replace("\\", "/").split("/")


def load_allowlist():
    allowed = set()
    if os.path.exists(ALLOWLIST):
        for raw in open(ALLOWLIST, encoding="utf-8"):
            entry = raw.split("#", 1)[0].strip()
            if entry:
                allowed.add(entry)
    return allowed


def first_client_ref(path):
    in_block_comment = False
    with open(path, encoding="utf-8") as f:
        for number, line in enumerate(f, 1):
            stripped = line.strip()
            if in_block_comment:
                if "*/" in stripped:
                    in_block_comment = False
                continue
            if stripped.startswith("/*"):
                in_block_comment = "*/" not in stripped
                continue
            if stripped.startswith("//") or stripped.startswith("*"):
                continue
            if CLIENT_REF.search(line.split("//", 1)[0]):
                return number, stripped
    return None


def main():
    allowed = load_allowlist()
    offenders = {}
    for root in ROOTS:
        for dirpath, _, filenames in os.walk(root):
            for filename in filenames:
                if not filename.endswith(".java"):
                    continue
                path = os.path.join(dirpath, filename).replace("\\", "/")
                rel = path[len(root) + 1:]
                if is_client(rel):
                    continue
                found = first_client_ref(path)
                if found:
                    offenders[path] = found

    new = {p: v for p, v in offenders.items() if p not in allowed}
    for path, (line, text) in sorted(new.items()):
        github("error", "Client-only code used outside a client package. A dedicated server can crash if it "
                        "loads this class. Move the client part into a client package, or if it really is only "
                        f"reached on the client, add the file to {ALLOWLIST} with a reason. ({text})", path, line)

    fixed = sorted(p for p in allowed if p not in offenders)
    for path in fixed:
        github("warning", f"{path} no longer uses client code. Remove it from {ALLOWLIST}.")

    print(f"{len(offenders)} file(s) outside client packages use client code: "
          f"{len(offenders) - len(new)} allowed, {len(new)} new.")
    return 1 if new else 0


if __name__ == "__main__":
    sys.exit(main())
