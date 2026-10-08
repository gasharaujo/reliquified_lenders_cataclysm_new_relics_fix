package dev.codex.rlcnrf.legacy;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LegacyDataComponentRegistry {
    private static final DeferredRegister<DataComponentType<?>> COMPONENTS =
        DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, "reliquified_lenders_cataclysm_new_relics_fix");

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIME = registerInt("legacy_time");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> COUNT = registerInt("legacy_count");
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE = registerInt("legacy_charge");

    private LegacyDataComponentRegistry() {
    }

    private static DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> registerInt(String id) {
        return COMPONENTS.register(id, () -> DataComponentType.<Integer>builder().persistent(Codec.INT).build());
    }

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}

