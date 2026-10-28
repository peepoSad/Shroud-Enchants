
package com.shroud.enchantments;

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

@EventBusSubscriber(modid = ShroudMod.MODID)
public class ShroudEvents {

    private static final ResourceKey<Enchantment> SHROUD_KEY =
            ResourceKey.create(
                    Registries.ENCHANTMENT,
                    ResourceLocation.fromNamespaceAndPath("shroud", "shroud")
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
    }
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
            ShroudMod.LOGGER.info(
        "Shroud anvil debug: left={}, right={}",
        event.getLeft(),
        event.getRight()
);
        ItemStack target = event.getLeft();
        ItemStack book = event.getRight();

        // Only handle enchanted books.
        if (target.isEmpty() || !book.is(net.minecraft.world.item.Items.ENCHANTED_BOOK)) {
            return;
    }

        var enchantmentRegistry = event.getPlayer().level()
                .registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT);

        Holder<Enchantment> shroud =
                enchantmentRegistry.get(SHROUD_KEY).orElse(null);

        if (shroud == null) {
            return;
    }

        // Check the enchantments stored inside the book.
        int shroudLevel =
                EnchantmentHelper.getEnchantmentsForCrafting(book).getLevel(shroud);

        if (shroudLevel <= 0) {
            return;
        }

        // Shroud books may only be applied to swords and bows.
        boolean allowed =
                target.getItem() instanceof SwordItem
                || target.getItem() instanceof BowItem;

        if (!allowed) {
            event.setOutput(ItemStack.EMPTY);
                ShroudMod.LOGGER.info(
        "Shroud anvil debug: blocking incompatible target {}",
        target.getItem()
);
            event.setCanceled(true);
        }
}
}
