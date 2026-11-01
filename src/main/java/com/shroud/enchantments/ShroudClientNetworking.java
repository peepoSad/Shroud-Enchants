
package com.shroud.enchantments;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
        modid = ShroudMod.MODID,
        bus = EventBusSubscriber.Bus.MOD
)
public class ShroudClientNetworking {

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(
                ShroudVisibilityPayload.TYPE,
                ShroudVisibilityPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() ->
                            ShroudClientState.setHidden(
                                    payload.playerId(),
                                    payload.active()
                            )
                    );
                }
        );
    }
}
