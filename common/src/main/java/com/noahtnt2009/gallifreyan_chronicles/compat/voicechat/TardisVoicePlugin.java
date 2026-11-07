package com.noahtnt2009.gallifreyan_chronicles.compat.voicechat;

import com.noahtnt2009.gallifreyan_chronicles.Constants;
import com.noahtnt2009.gallifreyan_chronicles.tardis.ecs.component.DoorState;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;
import de.maxhenkel.voicechat.api.packets.LocationalSoundPacket;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TardisVoicePlugin implements VoicechatPlugin {
    private static final float SAMPLE_RATE = 48000.0F;

    private static final float CLOSED_CUTOFF = 450.0F;
    private static final float CLOSED_GAIN = 0.55F;
    private static final float ONE_OPEN_CUTOFF = 2200.0F;
    private static final float ONE_OPEN_GAIN = 0.85F;
    private static final float BOTH_OPEN_CUTOFF = 7000.0F;
    private static final float BOTH_OPEN_GAIN = 1.0F;

    private static final double WHISPER_RANGE_FACTOR = 0.5;
    private static final double MAX_HEARING_RANGE = 26;

    private static final float FAR_VOLUME = 0.2F;

    private final Map<UUID, Stream> streams = new ConcurrentHashMap<>();
    private volatile VoicechatServerApi api;

    @Override
    public String getPluginId() {
        return Constants.MOD_ID + "_tardis";
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(VoicechatServerStartedEvent.class, event -> {
            api = event.getVoicechat();
            TardisVoiceLinks.setActive(true);
        });
        registration.registerEvent(VoicechatServerStoppedEvent.class, event -> {
            TardisVoiceLinks.setActive(false);
            streams.values().forEach(Stream::close);
            streams.clear();
            api = null;
        });
        registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophone);
        registration.registerEvent(PlayerDisconnectedEvent.class, event -> closeStreamsOf(event.getPlayerUuid()));
    }

    private void onMicrophone(MicrophonePacketEvent event) {
        VoicechatServerApi voicechat = api;
        VoicechatConnection senderConnection = event.getSenderConnection();
        if (voicechat == null || senderConnection == null) return;
        if (senderConnection.isDisabled() || senderConnection.isInGroup()) return;
        if (!(senderConnection.getPlayer().getPlayer() instanceof ServerPlayer sender)) return;

        MicrophonePacket packet = event.getPacket();
        byte[] data = packet.getOpusEncodedData();
        boolean endOfSpeech = data == null || data.length == 0;

        double range = Math.min(voicechat.getBroadcastRange(), MAX_HEARING_RANGE);
        if (packet.isWhispering()) range *= WHISPER_RANGE_FACTOR;

        UUID senderId = sender.getUUID();
        var senderDimension = sender.level().dimension();
        double sx = sender.getX();
        double sy = sender.getY();
        double sz = sender.getZ();

        for (TardisVoiceLinks.Link link : TardisVoiceLinks.links()) {
            if (link.exteriorDimension().equals(senderDimension)) {
                relay(voicechat, packet, data, endOfSpeech, range, senderId, sx, sy, sz, link, true);
            }
            if (link.interiorDimension().equals(senderDimension)) {
                relay(voicechat, packet, data, endOfSpeech, range, senderId, sx, sy, sz, link, false);
            }
        }
    }

    private void relay(VoicechatServerApi voicechat, MicrophonePacket packet, byte[] data, boolean endOfSpeech,
                       double range, UUID senderId, double sx, double sy, double sz,
                       TardisVoiceLinks.Link link, boolean towardsInterior) {
        double srcX = towardsInterior ? link.exteriorX() : link.doorX();
        double srcY = towardsInterior ? link.exteriorY() : link.doorY();
        double srcZ = towardsInterior ? link.exteriorZ() : link.doorZ();
        double dstX = towardsInterior ? link.doorX() : link.exteriorX();
        double dstY = towardsInterior ? link.doorY() : link.exteriorY();
        double dstZ = towardsInterior ? link.doorZ() : link.exteriorZ();
        var listenerDimension = towardsInterior ? link.interiorDimension() : link.exteriorDimension();

        double distance = Math.sqrt(sq(sx - srcX) + sq(sy - srcY) + sq(sz - srcZ));
        double remaining = range - distance;
        UUID key = streamKey(senderId, link, towardsInterior);
        if (remaining < 1.0) {
            Stream old = streams.remove(key);
            if (old != null) old.close();
            return;
        }

        Stream stream = streams.computeIfAbsent(key, k -> new Stream(voicechat));
        byte[] out;
        if (endOfSpeech) {
            out = new byte[0];
            stream.reset();
        } else {
            float closeness = (float) Math.max(0.0, Math.min(1.0, remaining / range));
            float proximity = FAR_VOLUME + (1.0F - FAR_VOLUME) * (float) Math.pow(closeness, 1.5);
            out = stream.process(data, link.doorState(), proximity, !towardsInterior);
            if (out == null) return;
        }

        LocationalSoundPacket sound = packet.locationalSoundPacketBuilder()
                .channelId(stream.channelId)
                .sender(senderId)
                .opusEncodedData(out)
                .sequenceNumber(stream.sequence++)
                .position(voicechat.createPosition(dstX, dstY, dstZ))
                .distance((float) remaining)
                .build();

        for (TardisVoiceLinks.PlayerPos listener : TardisVoiceLinks.players()) {
            if (!listener.dimension().equals(listenerDimension) || listener.id().equals(senderId)) continue;
            double d = Math.sqrt(sq(listener.x() - dstX) + sq(listener.y() - dstY) + sq(listener.z() - dstZ));
            if (d > remaining) continue;
            VoicechatConnection connection = voicechat.getConnectionOf(listener.id());
            if (connection == null || !connection.isConnected() || connection.isDisabled()) continue;
            voicechat.sendLocationalSoundPacketTo(connection, sound);
        }
    }

    private static double sq(double v) {
        return v * v;
    }

    private static UUID streamKey(UUID sender, TardisVoiceLinks.Link link, boolean towardsInterior) {
        long salt = towardsInterior ? 0L : 0x5DEECE66DL;
        return new UUID(sender.getMostSignificantBits() ^ link.tardisId().getLeastSignificantBits() ^ salt,
                sender.getLeastSignificantBits() ^ link.tardisId().getMostSignificantBits());
    }

    private void closeStreamsOf(UUID sender) {
        for (TardisVoiceLinks.Link link : TardisVoiceLinks.links()) {
            for (boolean towardsInterior : new boolean[]{true, false}) {
                Stream stream = streams.remove(streamKey(sender, link, towardsInterior));
                if (stream != null) stream.close();
            }
        }
    }

    private static final class Stream {
        final UUID channelId = UUID.randomUUID();
        long sequence;
        private final OpusDecoder decoder;
        private final OpusEncoder encoder;
        private final VoicechatServerApi api;
        private float lowPassA;
        private float lowPassB;

        Stream(VoicechatServerApi api) {
            this.api = api;
            this.decoder = api.createDecoder();
            this.encoder = api.createEncoder();
        }

        byte[] process(byte[] opus, DoorState doorState, float proximity, boolean clearWhenOpen) {
            if (decoder.isClosed() || encoder.isClosed()) return null;
            short[] samples = decoder.decode(opus);
            if (samples == null) return null;

            float cutoff;
            float gain;
            if (clearWhenOpen && !doorState.isClosed()) {
                cutoff = SAMPLE_RATE / 2.0F;
                gain = BOTH_OPEN_GAIN;
            } else if (doorState.isBothOpen()) {
                cutoff = BOTH_OPEN_CUTOFF;
                gain = BOTH_OPEN_GAIN;
            } else if (doorState.isClosed()) {
                cutoff = CLOSED_CUTOFF;
                gain = CLOSED_GAIN;
            } else {
                cutoff = ONE_OPEN_CUTOFF;
                gain = ONE_OPEN_GAIN;
            }
            float alpha = 1.0F - (float) Math.exp(-2.0 * Math.PI * cutoff / SAMPLE_RATE);
            for (int i = 0; i < samples.length; i++) {
                float value;
                if (cutoff >= SAMPLE_RATE / 2.0F) {
                    lowPassA = samples[i];
                    lowPassB = samples[i];
                    value = samples[i] * gain * proximity;
                } else {
                    lowPassA += alpha * (samples[i] - lowPassA);
                    lowPassB += alpha * (lowPassA - lowPassB);
                    value = lowPassB * gain * proximity;
                }
                samples[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(value)));
            }
            return encoder.encode(samples);
        }

        void reset() {
            lowPassA = 0.0F;
            lowPassB = 0.0F;
            if (!decoder.isClosed()) decoder.resetState();
            if (!encoder.isClosed()) encoder.resetState();
        }

        void close() {
            decoder.close();
            encoder.close();
        }
    }
}
