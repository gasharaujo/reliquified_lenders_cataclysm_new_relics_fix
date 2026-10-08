package dev.codex.rlcnrf.mixin;

import dev.codex.rlcnrf.legacy.data.RelicData;
import it.hurts.sskirillss.relics.api.relics.RelicTemplate;
import java.lang.reflect.InvocationTargetException;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
    targets = {
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.back.VoidCloakItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.ScouringEyeItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.VoidVortexInBottleItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.hands.VacuumGloveItem",
        "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.head.VoidBubbleItem"
    },
    remap = false
)
public abstract class RelicDefinitionCompatMixin {
    public RelicTemplate constructDefaultRelicTemplate() {
        try {
            Object value = getClass().getMethod("constructDefaultRelicData").invoke(this);
            if (value instanceof RelicData data) {
                return data.toTemplate();
            }
            throw new IllegalStateException("Unexpected legacy relic definition: " + value);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
            throw new IllegalStateException("Failed to translate relic definition for " + getClass().getName(), exception);
        }
    }
}
