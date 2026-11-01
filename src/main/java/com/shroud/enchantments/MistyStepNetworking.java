
package com.shroud.enchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public class MistyStepNetworking {

    private static final ResourceKey<Enchantment> MISTY_STEP_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath(
                            ShroudMod.MODID,
                            "misty_step"
                    )
            );

    private static final double MAX_RANGE = 30.0;
    private static final double STEP_SIZE = 0.1;
    private static final double MIN_DISTANCE = 0.5;

    private static final Map<UUID, Long> LAST_USE_TICK =
            new HashMap<>();

    @SubscribeEvent
    public static void registerPayloads(
            RegisterPayloadHandlersEvent event
    ) {
        event.registrar("1").playToServer(
                MistyStepPayload.TYPE,
                MistyStepPayload.STREAM_CODEC,
                MistyStepNetworking::handleMistyStep
        );
    }

    private static void handleMistyStep(
            MistyStepPayload payload,
            IPayloadContext context
    ) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        ServerLevel level = player.serverLevel();


        UUID playerId = player.getUUID();



        // Check Misty Step on equipped boots.
        var enchantmentRegistry = level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        Holder<Enchantment> mistyStep =
                enchantmentRegistry.get(MISTY_STEP_KEY).orElse(null);

        if (mistyStep == null) {
            return;
        }

        ItemStack boots = player.getItemBySlot(
                EquipmentSlot.FEET
        );

        if (boots.getEnchantmentLevel(mistyStep) <= 0) {
            return;
        }

        // Find an Ender Pearl.
        ItemStack pearlStack = findPearl(player);

        if (pearlStack == null) {
            return;
        }

        // Calculate the destination.
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookDirection = player.getLookAngle();

        Vec3 rayEnd = eyePosition.add(
                lookDirection.scale(MAX_RANGE)
        );

        BlockHitResult hitResult = level.clip(
                new ClipContext(
                        eyePosition,
                        rayEnd,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        player
                )
        );

        // If a block obstructs the path, stop before it.
        double maxDistance = MAX_RANGE;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            maxDistance = eyePosition.distanceTo(
                    hitResult.getLocation()
            ) - 0.1;
        }

        // Search backwards for the furthest safe position.
        Vec3 destination = null;

        for (double distance = maxDistance;
                distance >= MIN_DISTANCE;
                distance -= STEP_SIZE) {

            Vec3 offset = lookDirection.scale(distance);

            Vec3 candidate = player.position().add(offset);

            AABB destinationBox =
                    player.getBoundingBox().move(offset);

            // Make sure the entire player hitbox fits.
            if (level.noCollision(player, destinationBox)) {
                destination = candidate;
                break;
            }
        }

        // No safe destination found.
        if (destination == null) {
            return;
        }

        // Perform the teleport.
        Vec3 departurePosition = player.position();
        // Departure particles.
level.sendParticles(
        ParticleTypes.PORTAL,
        departurePosition.x,
        departurePosition.y + player.getBbHeight() / 2.0,
        departurePosition.z,
        80,
        0.5,
        0.8,
        0.5,
        0.2
);

// Departure sound.
level.playSound(
        null,
        departurePosition.x,
        departurePosition.y,
        departurePosition.z,
        SoundEvents.ENDERMAN_TELEPORT,
        SoundSource.PLAYERS,
        1.0F,
        1.0F
);
        boolean successful = player.teleportTo(
                level,
                destination.x,
                destination.y,
                destination.z,
                Set.of(),
                player.getYRot(),
                player.getXRot()
        );

        if (!successful) {
            return;
        }
// Arrival particles.
player.serverLevel().sendParticles(
        ParticleTypes.PORTAL,
        player.getX(),
        player.getY() + player.getBbHeight() / 2.0,
        player.getZ(),
        100,
        0.5,
        0.8,
        0.5,
        0.2
);

// Arrival sound.
player.serverLevel().playSound(
        null,
        player.getX(),
        player.getY(),
        player.getZ(),
        SoundEvents.ENDERMAN_TELEPORT,
        SoundSource.PLAYERS,
        1.0F,
        1.0F
);

// Consume one Ender Pearl after successful teleportation.
pearlStack.shrink(1);
player.getInventory().setChanged();

ShroudMod.LOGGER.info("Misty Step arrival effects triggered!");
}
private static ItemStack findPearl(ServerPlayer player) {
    for (int i = 0;
            i < player.getInventory().getContainerSize();
            i++) {

        ItemStack stack = player.getInventory().getItem(i);

        if (stack.is(Items.ENDER_PEARL)) {
            return stack;
        }
    }

    return null;
}
}