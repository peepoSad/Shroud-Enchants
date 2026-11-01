
package com.shroud.enchantments;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = ShroudMod.MODID)
public class ShroudEvents {
        private static final Map<UUID, Integer> SHROUD_TICKS = new HashMap<>();

    private static final ResourceKey<Enchantment> SHROUD_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath("shroud", "shroud")
            );
    private static final ResourceKey<Enchantment> MISTY_STEP_KEY =
        ResourceKey.create(
                Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath("shroud", "misty_step")
        );

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof Mob)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();

        if (!(attacker instanceof Player player)) {
            return;
        }

        var enchantmentRegistry = player.level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        Holder<Enchantment> shroud =
                enchantmentRegistry.get(SHROUD_KEY).orElse(null);

        if (shroud == null) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();

        // Only swords and bows may activate Shroud.
        if (!(weapon.getItem() instanceof
                net.minecraft.world.item.SwordItem)
                && !(weapon.getItem() instanceof
                net.minecraft.world.item.BowItem)) {
            return;
        }

        int level = weapon.getEnchantmentLevel(shroud);

        if (level <= 0) {
            return;
        }

        int durationTicks = level * 20;

        player.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                durationTicks,
                0,
                false,
                false,
                true
        ));
        // Mark this player as being under Shroud.
        SHROUD_TICKS.put(player.getUUID(), durationTicks);
        // Tell every connected client that this player is hidden.
        PacketDistributor.sendToAllPlayers(
                new ShroudVisibilityPayload(player.getUUID(), true)
);

        // Make nearby mobs immediately lose this player as a target.
        for (Mob mob : player.level().getEntitiesOfClass(
                Mob.class,
                player.getBoundingBox().inflate(64)
        )) {
        if (mob.getTarget() == player) {
                mob.setTarget(null);
        }
        }
    }

        @SubscribeEvent
        public static void onMobChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) {
                return;
        }

        if (SHROUD_TICKS.containsKey(player.getUUID())) {
                event.setCanceled(true);
        }
        }

        @SubscribeEvent
        public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
                return;
        }

        UUID playerId = player.getUUID();
        Integer remaining = SHROUD_TICKS.get(playerId);

        if (remaining == null) {
                return;
        }

        if (remaining <= 1) {
                SHROUD_TICKS.remove(playerId);

                // Tell every connected client that Shroud has expired.
                PacketDistributor.sendToAllPlayers(
                        new ShroudVisibilityPayload(playerId, false)
                );
        } else {
                SHROUD_TICKS.put(playerId, remaining - 1);
        }
        }

    
        @SubscribeEvent
        public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack target = event.getLeft();
        ItemStack book = event.getRight();

        // Only handle enchanted books.
        if (target.isEmpty()
                || !book.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
                return;
        }

        var enchantmentRegistry = event.getPlayer().level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        Holder<Enchantment> shroud =
                enchantmentRegistry.get(SHROUD_KEY).orElse(null);

        Holder<Enchantment> mistyStep =
                enchantmentRegistry.get(MISTY_STEP_KEY).orElse(null);

        var bookEnchantments =
                EnchantmentHelper.getEnchantmentsForCrafting(book);

        // Check Shroud compatibility.
        if (shroud != null && bookEnchantments.getLevel(shroud) > 0) {
                boolean allowed =
                        target.getItem() instanceof SwordItem
                        || target.getItem() instanceof BowItem;

                if (!allowed) {
                event.setOutput(ItemStack.EMPTY);
                event.setCanceled(true);
                return;
                }
        }

        // Check Misty Step compatibility.
        if (mistyStep != null && bookEnchantments.getLevel(mistyStep) > 0) {
                boolean allowed =
                        target.getItem() instanceof net.minecraft.world.item.ArmorItem armor
                        && armor.getEquipmentSlot()
                                == net.minecraft.world.entity.EquipmentSlot.FEET;

                if (!allowed) {
                event.setOutput(ItemStack.EMPTY);
                event.setCanceled(true);
                }
        }
        }

}
