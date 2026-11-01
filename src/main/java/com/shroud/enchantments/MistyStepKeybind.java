
package com.shroud.enchantments;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        value = Dist.CLIENT
)
public class MistyStepKeybind {

    public static final KeyMapping MISTY_STEP_KEY =
            new KeyMapping(
                    "key.shroud.misty_step",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_G,
                    "key.categories.misc"
            );

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(MISTY_STEP_KEY);
    }
@SubscribeEvent
public static void onClientTick(ClientTickEvent.Post event) {
    while (MISTY_STEP_KEY.consumeClick()) {
        if (Minecraft.getInstance().player != null) {
            PacketDistributor.sendToServer(
                    new MistyStepPayload()
            );
        }
    }
}
}
