
package com.shroud.enchantments;

import java.util.UUID;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ShroudVisibilityPayload(
        UUID playerId,
        boolean active
) implements CustomPacketPayload {

    public static final Type<ShroudVisibilityPayload> TYPE =
            new Type<>(
                    ResourceLocation.fromNamespaceAndPath(
                            ShroudMod.MODID,
                            "shroud_visibility"
                    )
            );

    public static final StreamCodec<ByteBuf, ShroudVisibilityPayload> STREAM_CODEC =
            StreamCodec.composite(
                    UUIDUtil.STREAM_CODEC,
                    ShroudVisibilityPayload::playerId,
                    ByteBufCodecs.BOOL,
                    ShroudVisibilityPayload::active,
                    ShroudVisibilityPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
