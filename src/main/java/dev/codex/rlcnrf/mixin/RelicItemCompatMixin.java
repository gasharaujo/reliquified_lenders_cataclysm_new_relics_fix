package dev.codex.rlcnrf.mixin;

import com.google.common.collect.Multimap;
import dev.codex.rlcnrf.legacy.LegacyRelicItem;
import dev.codex.rlcnrf.legacy.component.LegacyAbilitiesComponent;
import dev.codex.rlcnrf.legacy.component.LegacyAbilityComponent;
import dev.codex.rlcnrf.legacy.component.LegacyLevelingComponent;
import dev.codex.rlcnrf.legacy.data.RelicData;
import dev.codex.rlcnrf.legacy.data.cast.misc.CastStage;
import dev.codex.rlcnrf.legacy.data.cast.misc.CastType;
import dev.codex.rlcnrf.legacy.data.leveling.AbilitiesData;
import it.hurts.sskirillss.relics.api.relics.abilities.activation.AbilityActivationContext;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicAttributeModifier;
import it.hurts.sskirillss.relics.items.relics.base.data.RelicSlotModifier;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import top.theillusivec4.curios.api.SlotContext;

@Mixin(targets = "it.hurts.octostudios.reliquified_lenders_cataclysm.items.base.RECItem", remap = false)
public abstract class RelicItemCompatMixin implements LegacyRelicItem {
    /*
     * These forwarders deliberately exist as concrete methods on RECItem after
     * Mixin application. Interface defaults alone are insufficient when multiple
     * legacy add-on bridges add unrelated ICurioItem subinterfaces to RelicItem:
     * the JVM can otherwise report AbstractMethodError for the conflicting default.
     */
    @Override
    public RelicData constructLegacyRelicData() {
        return LegacyRelicItem.super.constructLegacyRelicData();
    }

    @Override
    public AbilitiesData getAbilitiesData() {
        return LegacyRelicItem.super.getAbilitiesData();
    }

    @Override
    public void castActiveAbility(ItemStack stack, Player player, String ability, CastType type, CastStage stage) {
        LegacyRelicItem.super.castActiveAbility(stack, player, ability, type, stage);
    }

    @Override
    public void activateAbility(AbilityActivationContext context) {
        LegacyRelicItem.super.activateAbility(context);
    }

    @Override
    public RelicAttributeModifier getRelicAttributeModifiers(ItemStack stack) {
        return LegacyRelicItem.super.getRelicAttributeModifiers(stack);
    }

    @Override
    public RelicSlotModifier getSlotModifiers(ItemStack stack) {
        return LegacyRelicItem.super.getSlotModifiers(stack);
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
        SlotContext slotContext, ResourceLocation id, ItemStack stack
    ) {
        return LegacyRelicItem.super.getAttributeModifiers(slotContext, id, stack);
    }

    @Override
    public it.hurts.sskirillss.relics.api.relics.data.RelicData current(ItemStack stack) {
        return LegacyRelicItem.super.current(stack);
    }

    @Override
    public double getStatValue(ItemStack stack, String ability, String stat) {
        return LegacyRelicItem.super.getStatValue(stack, ability, stat);
    }

    @Override
    public double getStatValue(ItemStack stack, String ability, String stat, int level) {
        return LegacyRelicItem.super.getStatValue(stack, ability, stat, level);
    }

    @Override
    public int getStatQuality(ItemStack stack, String ability, String stat) {
        return LegacyRelicItem.super.getStatQuality(stack, ability, stat);
    }

    @Override
    public double getStatValueByQuality(String ability, String stat, int quality) {
        return LegacyRelicItem.super.getStatValueByQuality(ability, stat, quality);
    }

    @Override
    public void setStatInitialValue(ItemStack stack, String ability, String stat, double value) {
        LegacyRelicItem.super.setStatInitialValue(stack, ability, stat, value);
    }

    @Override
    public int getAbilityLevel(ItemStack stack, String ability) {
        return LegacyRelicItem.super.getAbilityLevel(stack, ability);
    }

    @Override
    public int getAbilityMaxLevel(ItemStack stack, String ability) {
        return LegacyRelicItem.super.getAbilityMaxLevel(stack, ability);
    }

    @Override
    public boolean isAbilityUnlocked(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityUnlocked(stack, ability);
    }

    @Override
    public boolean isAbilityEnabled(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityEnabled(stack, ability);
    }

    @Override
    public boolean isAbilityTicking(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityTicking(stack, ability);
    }

    @Override
    public int getAbilityCooldown(ItemStack stack, String ability) {
        return LegacyRelicItem.super.getAbilityCooldown(stack, ability);
    }

    @Override
    public void setAbilityCooldown(ItemStack stack, String ability, int ticks) {
        LegacyRelicItem.super.setAbilityCooldown(stack, ability, ticks);
    }

    @Override
    public boolean isAbilityOnCooldown(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityOnCooldown(stack, ability);
    }

    @Override
    public boolean isAbilityMaxLevel(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityMaxLevel(stack, ability);
    }

    @Override
    public boolean isAbilityFlawless(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityFlawless(stack, ability);
    }

    @Override
    public boolean mayUpgrade(ItemStack stack, String ability) {
        return LegacyRelicItem.super.mayUpgrade(stack, ability);
    }

    @Override
    public boolean mayReset(ItemStack stack, String ability) {
        return LegacyRelicItem.super.mayReset(stack, ability);
    }

    @Override
    public boolean isAbilityUpgradeEnabled(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityUpgradeEnabled(stack, ability);
    }

    @Override
    public boolean isAbilityResetEnabled(ItemStack stack, String ability) {
        return LegacyRelicItem.super.isAbilityResetEnabled(stack, ability);
    }

    @Override
    public LegacyAbilitiesComponent getAbilitiesComponent(ItemStack stack) {
        return LegacyRelicItem.super.getAbilitiesComponent(stack);
    }

    @Override
    public void setAbilitiesComponent(ItemStack stack, LegacyAbilitiesComponent component) {
        LegacyRelicItem.super.setAbilitiesComponent(stack, component);
    }

    @Override
    public LegacyAbilityComponent getAbilityComponent(ItemStack stack, String ability) {
        return LegacyRelicItem.super.getAbilityComponent(stack, ability);
    }

    @Override
    public void setAbilityComponent(ItemStack stack, String ability, LegacyAbilityComponent component) {
        LegacyRelicItem.super.setAbilityComponent(stack, ability, component);
    }

    @Override
    public LegacyLevelingComponent getLevelingComponent(ItemStack stack) {
        return LegacyRelicItem.super.getLevelingComponent(stack);
    }

    @Override
    public void setLevelingComponent(ItemStack stack, LegacyLevelingComponent component) {
        LegacyRelicItem.super.setLevelingComponent(stack, component);
    }

    @Override
    public void spreadRelicExperience(LivingEntity entity, ItemStack stack, int amount) {
        LegacyRelicItem.super.spreadRelicExperience(entity, stack, amount);
    }

    @Override
    public void spreadRelicExperience(LivingEntity entity, ItemStack stack, int amount, double multiplier) {
        LegacyRelicItem.super.spreadRelicExperience(entity, stack, amount, multiplier);
    }
}
