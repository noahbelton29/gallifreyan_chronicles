package uk.nbforge.gallifreyan_chronicles.client.protection.build;

import uk.nbforge.gallifreyan_chronicles.util.GCLog;

import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * logs who's playing (account id, name, build id, jar hash) every time someone launches this build. also notices if a
 * screen recorder or screenshot program is running, cant tell if its actually recording tho. all goes to
 * gc_tester_log.jsonl in the game folder, and if report_url is set in gc_build.properties each line gets sent there too
 */
public final class TesterReport {
    private static final String REPORT_URL = "";
    private static final String RESOURCE = "/gc_build.properties";
    private static final String LOG_FILE = "gc_tester_log.jsonl";
    private static final long CHECK_EVERY_SECONDS = 5;

    private static final Set<String> RECORDERS = Set.of(
            "obs64.exe", "obs32.exe", "obs.exe", "streamlabs obs.exe",
            "bandicam.exe", "bdcam.exe", "sharex.exe", "medal.exe", "fraps.exe",
            "action_x64.exe", "camtasiastudio.exe", "camrecorder.exe",
            "screentogif.exe", "snagit32.exe", "snagiteditor.exe", "snagitcapture.exe",
            "snippingtool.exe", "screenclippinghost.exe", "screensketch.exe", "clipchamp.exe", "lightshot.exe", "greenshot.exe",
            "xsplit.core.exe", "flashbackrecorder.exe", "loom.exe");

    private static String url;
    private static String buildId = "-";
    private static String jarHash = "-";
    private static String jarName = "-";

    private TesterReport() {
    }

    public static void start(UUID account, String name) {
        loadSettings();
        GCLog.TESTER.info("Tester report on, logging to {} (uuid, name, build id, jar hash and running recorders)", LOG_FILE);

        Thread thread = new Thread(() -> run(account, name), "GC Tester Report");
        thread.setDaemon(true);
        thread.start();
    }

    private static void run(UUID account, String name) {
        readJar();
        send(account, name, "session", Set.of());
        Set<String> seen = new TreeSet<>();
        while (true) {
            Set<String> now = runningRecorders();
            if (!now.isEmpty() && !seen.containsAll(now)) {
                seen.addAll(now);
                send(account, name, "recorder", now);
            }
            try {
                Thread.sleep(CHECK_EVERY_SECONDS * 1000L);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static Set<String> runningRecorders() {
        Set<String> found = new TreeSet<>();
        ProcessHandle.allProcesses().forEach(process -> process.info().command().ifPresent(command -> {
            String file = Path.of(command).getFileName().toString().toLowerCase(Locale.ROOT);
            if (RECORDERS.contains(file)) found.add(file);
        }));
        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")) {
            try {
                Process tasklist = new ProcessBuilder("tasklist", "/fo", "csv", "/nh").redirectErrorStream(true).start();
                try (var reader = new BufferedReader(new InputStreamReader(tasklist.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (!line.startsWith("\"")) continue;
                        int end = line.indexOf('"', 1);
                        if (end <= 1) continue;
                        String file = line.substring(1, end).toLowerCase(Locale.ROOT);
                        if (RECORDERS.contains(file)) found.add(file);
                    }
                }
                tasklist.waitFor();
            } catch (Exception ignored) {
            }
        }
        return found;
    }

    private static void loadSettings() {
        url = REPORT_URL;
        try (InputStream in = TesterReport.class.getResourceAsStream(RESOURCE)) {
            if (in == null) return;
            Properties props = new Properties();
            props.load(in);
            buildId = props.getProperty("build_id", "-").trim();
            String fromBuild = props.getProperty("report_url", "").trim();
            if (!fromBuild.isEmpty()) url = fromBuild;
        } catch (Exception ignored) {
        }
    }

    /** sha-256 of the mod jar so u know exactly which file someone has */
    private static void readJar() {
        try {
            Path jar = Path.of(TesterReport.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!Files.isRegularFile(jar)) return;
            jarName = jar.getFileName().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            jarHash = HexFormat.of().formatHex(digest.digest(Files.readAllBytes(jar)));
        } catch (Exception ignored) {
        }
    }

    private static void send(UUID account, String name, String event, Set<String> recorders) {
        String json = "{"
                + "\"event\":" + quote(event)
                + ",\"uuid\":" + quote(String.valueOf(account))
                + ",\"name\":" + quote(name)
                + ",\"build_id\":" + quote(buildId)
                + ",\"jar\":" + quote(jarName)
                + ",\"jar_sha256\":" + quote(jarHash)
                + ",\"recorders\":[" + String.join(",", recorders.stream().map(TesterReport::quote).toList()) + "]"
                + ",\"time\":" + quote(Instant.now().toString())
                + "}";
        writeToFile(json);
        if (url == null || url.isBlank()) return;
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            client.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (Exception e) {
            GCLog.TESTER.warn("Couldn't send the tester report: {}", e.toString());
        }
    }

    private static synchronized void writeToFile(String json) {
        try {
            Path file = Minecraft.getInstance().gameDirectory.toPath().resolve(LOG_FILE);
            Files.writeString(file, json + System.lineSeparator(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            GCLog.TESTER.warn("Couldn't write the tester log: {}", e.toString());
        }
    }

    private static String quote(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : String.valueOf(value).toCharArray()) {
            if (c == '"' || c == '\\') sb.append('\\').append(c);
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.append('"').toString();
    }
}
