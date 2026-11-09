package com.shroud.enchantments;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        value = Dist.CLIENT
)
public class ShroudClientNetworking {

    @SubscribeEvent
    public static void registerPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
                ShroudVisibilityPayload.TYPE,
                (payload, context) -> ShroudClientState.setHidden(
                        payload.playerId(),
                        payload.active()
                )
        );
    }
}
