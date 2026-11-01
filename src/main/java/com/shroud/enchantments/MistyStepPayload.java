
package com.shroud.enchantments;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public record MistyStepPayload() implements CustomPacketPayload {

    public static final Type<MistyStepPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            ShroudMod.MODID,
                            "misty_step"
                    )
            );

    public static final StreamCodec<ByteBuf, MistyStepPayload> STREAM_CODEC =
            StreamCodec.unit(new MistyStepPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
