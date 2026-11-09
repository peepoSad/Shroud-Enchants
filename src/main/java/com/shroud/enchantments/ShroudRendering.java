package com.shroud.enchantments;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
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
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            return;
        }

        AvatarRenderState renderState =
                (AvatarRenderState) event.getRenderState();

        Entity renderedEntity = minecraft.level.getEntity(renderState.id);

        if (renderedEntity != null
                && ShroudClientState.isHidden(renderedEntity.getUUID())) {
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
