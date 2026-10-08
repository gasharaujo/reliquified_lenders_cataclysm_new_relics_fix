package dev.codex.rlcnrf.legacy.network;

import dev.codex.rlcnrf.ReliquifiedLendersCataclysmNewRelicsFix;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Relics 0.10 packet surface removed in 0.12. */
public final class LegacyPacketPlayerMotion implements CustomPacketPayload {
    public static final Type<LegacyPacketPlayerMotion> TYPE = new Type<>(
        ResourceLocation.fromNamespaceAndPath(
            ReliquifiedLendersCataclysmNewRelicsFix.MOD_ID,
            "legacy_player_motion"
        )
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, LegacyPacketPlayerMotion> STREAM_CODEC =
        CustomPacketPayload.codec(LegacyPacketPlayerMotion::write, LegacyPacketPlayerMotion::new);

    private final double x;
    private final double y;
    private final double z;

    public LegacyPacketPlayerMotion(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    private LegacyPacketPlayerMotion(RegistryFriendlyByteBuf buffer) {
        this(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeDouble(x);
        buffer.writeDouble(y);
        buffer.writeDouble(z);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> context.player().setDeltaMovement(x, y, z));
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, LegacyPacketPlayerMotion::handle);
    }
}
