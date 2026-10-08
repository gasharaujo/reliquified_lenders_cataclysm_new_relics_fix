package dev.codex.rlcnrf.mixin;

import it.hurts.sskirillss.relics.utils.EntityUtils;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.SlotContext;

/** Gives the Void Cloak's void immunity an escape path instead of leaving its wearer falling forever. */
@Mixin(
    targets = "it.hurts.octostudios.reliquified_lenders_cataclysm.items.relics.back.VoidCloakItem",
    remap = false
)
public abstract class VoidCloakSafetyMixin {
    @Unique
    private static final String SAFE_POSITION = "rlcnrf:void_cloak_safe_position";
    @Unique
    private static final String SAFE_DIMENSION = "rlcnrf:void_cloak_safe_dimension";
    @Unique
    private static final ResourceLocation VOID_CLOAK = ResourceLocation.fromNamespaceAndPath(
        "reliquified_lenders_cataclysm", "void_cloak"
    );

    @Inject(method = "curioTick", at = @At("RETURN"))
    private void rlcnrf$rememberSafePosition(SlotContext slotContext, ItemStack stack, CallbackInfo callback) {
        if (!(slotContext.entity() instanceof ServerPlayer player) || !player.onGround()) {
            return;
        }

        CompoundTag data = ((IEntityExtension) player).getPersistentData();
        data.putLong(SAFE_POSITION, player.blockPosition().asLong());
        data.putString(SAFE_DIMENSION, player.level().dimension().location().toString());
    }

    @Inject(method = "onPlayerDamage", at = @At("HEAD"))
    private static void rlcnrf$rescueFromVoid(LivingIncomingDamageEvent event, CallbackInfo callback) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel currentLevel = player.serverLevel();
        if (player.getY() >= currentLevel.getMinBuildHeight() || !rlcnrf$hasVoidCloak(player)) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        CompoundTag data = ((IEntityExtension) player).getPersistentData();
        ServerLevel destination = null;
        BlockPos position = null;
        if (data.contains(SAFE_POSITION) && data.contains(SAFE_DIMENSION)) {
            ResourceLocation dimensionId = ResourceLocation.tryParse(data.getString(SAFE_DIMENSION));
            if (dimensionId != null) {
                ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, dimensionId);
                destination = server.getLevel(dimension);
                position = BlockPos.of(data.getLong(SAFE_POSITION));
            }
        }

        if (destination == null || position == null) {
            destination = server.overworld();
            position = destination.getSharedSpawnPos();
        }

        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.teleportTo(
            destination,
            position.getX() + 0.5D,
            position.getY() + 0.1D,
            position.getZ() + 0.5D,
            Set.<RelativeMovement>of(),
            player.getYRot(),
            player.getXRot()
        );
        event.setCanceled(true);
    }

    @Unique
    private static boolean rlcnrf$hasVoidCloak(ServerPlayer player) {
        Item cloak = BuiltInRegistries.ITEM.get(VOID_CLOAK);
        return cloak != null && !EntityUtils.findEquippedCurio(player, cloak).isEmpty();
    }
}
