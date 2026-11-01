
package com.shroud.enchantments;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        value = Dist.CLIENT
)
public class ShroudRendering {

    // Hide the entire player model, including armor and held items.
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (ShroudClientState.isHidden(event.getEntity().getUUID())) {
            event.setCanceled(true);
        }
    }

    // Hide your own hand and held item in first-person view.
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.player != null
                && ShroudClientState.isHidden(minecraft.player.getUUID())) {
            event.setCanceled(true);
        }
    }
}
