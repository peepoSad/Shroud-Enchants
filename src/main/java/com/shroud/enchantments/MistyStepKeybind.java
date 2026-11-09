package com.shroud.enchantments;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        value = Dist.CLIENT
)
public class MistyStepKeybind {

    public static final KeyMapping MISTY_STEP_KEY =
            new KeyMapping(
                    "key.shroud.misty_step",
                    InputConstants.KEY_G,
                    KeyMapping.Category.MISC
            );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MISTY_STEP_KEY);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (MISTY_STEP_KEY.consumeClick()) {
            if (Minecraft.getInstance().player != null) {
                ClientPacketDistributor.sendToServer(
                        new MistyStepPayload()
                );
            }
        }
    }
}
