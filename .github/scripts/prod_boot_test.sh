#!/usr/bin/env bash
set -euo pipefail

LOADER="$1"
MOD_JAR="$2"
SERVER_DIR="$3"
BOOT_FLAG="-Dgallifreyan_chronicles.bootTest=true"
PASS_LINE="BOOT TEST PASSED"
TIMEOUT_SECONDS=180

prop() { grep "^$1=" gradle.properties | head -n1 | cut -d= -f2-; }
MC="$(prop minecraft_version)"

modrinth() {
  local slug="$1" wanted="$2" out="$3"
  local versions
  versions="$(curl -fsSL -G "https://api.modrinth.com/v2/project/${slug}/version" \
    --data-urlencode "loaders=[\"${LOADER}\"]" \
    --data-urlencode "game_versions=[\"${MC}\"]")"
  local pick
  pick="$(echo "$versions" | jq -c --arg v "$wanted" \
    '(map(select(.version_number == $v or (.version_number | contains($v)))) | first) // first')"
  if [ -z "$pick" ] || [ "$pick" = "null" ]; then
    echo "::error::No ${slug} build on Modrinth for ${LOADER} ${MC}"
    exit 1
  fi
  local number url
  number="$(echo "$pick" | jq -r '.version_number')"
  url="$(echo "$pick" | jq -r '(.files | map(select(.primary)) | first // .files[0]).url')"
  if [[ "$number" != *"$wanted"* ]]; then
    echo "::warning::${slug} ${wanted} not found on Modrinth for ${LOADER} ${MC}, using ${number}"
  fi
  echo "  ${slug} ${number}"
  curl -fsSL -o "$out" "$url"
}

mkdir -p "$SERVER_DIR/mods"
rm -f "$SERVER_DIR"/mods/*.jar
cp "$MOD_JAR" "$SERVER_DIR/mods/"
echo "Mods:"
echo "  $(basename "$MOD_JAR")"
modrinth geckolib "$(prop geckolib_version)" "$SERVER_DIR/mods/geckolib.jar"

case "$LOADER" in
  fabric)
    modrinth fabric-api "$(prop fabric_version)" "$SERVER_DIR/mods/fabric-api.jar"
    if [ ! -f "$SERVER_DIR/fabric-server-launch.jar" ]; then
      installer="$(curl -fsSL https://meta.fabricmc.net/v2/versions/installer | jq -r 'map(select(.stable)) | first.version')"
      curl -fsSL -o "$SERVER_DIR/fabric-server-launch.jar" \
        "https://meta.fabricmc.net/v2/versions/loader/${MC}/$(prop fabric_loader_version)/${installer}/server/jar"
    fi
    START=(java -Xmx3G -jar fabric-server-launch.jar nogui)
    ;;
  neoforge)
    version="$(prop neoforge_version)"
    if [ ! -f "$SERVER_DIR/run.sh" ]; then
      curl -fsSL -o "$SERVER_DIR/installer.jar" \
        "https://maven.neoforged.net/releases/net/neoforged/neoforge/${version}/neoforge-${version}-installer.jar"
      (cd "$SERVER_DIR" && java -jar installer.jar --installServer > installer.log 2>&1) \
        || { echo "::error::NeoForge installer failed"; tail -50 "$SERVER_DIR/installer.log"; exit 1; }
      rm -f "$SERVER_DIR/installer.jar"
    fi
    START=(bash run.sh nogui)
    ;;
  *)
    echo "::error::Unknown loader ${LOADER}"
    exit 1
    ;;
esac

echo "eula=true" > "$SERVER_DIR/eula.txt"
printf 'online-mode=false\nspawn-protection=0\nmax-tick-time=-1\nsync-chunk-writes=false\npause-when-empty-seconds=-1\n' > "$SERVER_DIR/server.properties"
rm -rf "$SERVER_DIR/world" "$SERVER_DIR/crash-reports" "$SERVER_DIR/logs"

echo "Starting the ${LOADER} server..."
(cd "$SERVER_DIR" && JAVA_TOOL_OPTIONS="$BOOT_FLAG" timeout "$TIMEOUT_SECONDS" "${START[@]}" < /dev/null) 2>&1 | tee boot.log || true

if grep -q "$PASS_LINE" boot.log; then
  echo "The ${LOADER} server started and ticked with only the mod and its required libraries"
elif grep -q "Done (" boot.log && ! grep -q "Boot test: levels loaded" boot.log; then
  echo "::error::The ${LOADER} server started but the boot test hook never ran, so it was not shut down"
  exit 1
else
  echo "::error::The ${LOADER} server did not finish booting. Check boot.log and the crash reports in the artifacts."
  exit 1
fi
