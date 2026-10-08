package dev.codex.rlcnrf.mixin;

import it.hurts.sskirillss.relics.api.relics.IRelicItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

/** Restores the legacy base-item cooldown tick omitted by the 0.12 API bridge. */
@Mixin(
    targets = "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.charm.ScouringEyeItem",
    remap = false
)
public abstract class ScouringEyeCooldownMixin {
    private static final String PURSUIT = "glowing_scour";

    @Inject(method = "curioTick", at = @At("RETURN"))
    private void rlcnrf$tickPursuitCooldown(SlotContext slotContext, ItemStack stack, CallbackInfo callback) {
        if (slotContext.entity().level().isClientSide || !(stack.getItem() instanceof IRelicItem relic)) {
            return;
        }

        var ability = relic.getRelicData(slotContext.entity(), stack).getAbilitiesData().getAbilityData(PURSUIT);
        if (ability != null && ability.getActivationCooldown() > 0) {
            ability.addActivationCooldown(-1);
        }
    }
}
