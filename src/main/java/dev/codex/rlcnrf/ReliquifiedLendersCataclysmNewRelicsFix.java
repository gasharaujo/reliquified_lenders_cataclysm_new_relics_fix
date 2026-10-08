package dev.codex.rlcnrf;

import dev.codex.rlcnrf.legacy.LegacyDataComponentRegistry;
import dev.codex.rlcnrf.legacy.network.LegacyPacketPlayerMotion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(ReliquifiedLendersCataclysmNewRelicsFix.MOD_ID)
public final class ReliquifiedLendersCataclysmNewRelicsFix {
    public static final String MOD_ID = "reliquified_lenders_cataclysm_new_relics_fix";

    public ReliquifiedLendersCataclysmNewRelicsFix(IEventBus modBus) {
        LegacyDataComponentRegistry.register(modBus);
        modBus.addListener(LegacyPacketPlayerMotion::register);
    }
}
