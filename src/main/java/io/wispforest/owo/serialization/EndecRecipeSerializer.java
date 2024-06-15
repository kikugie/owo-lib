package io.wispforest.owo.serialization;

import com.mojang.serialization.MapCodec;
import io.wispforest.endec.Endec;
import io.wispforest.endec.SerializationAttributes;
import io.wispforest.endec.SerializationContext;
import io.wispforest.endec.StructEndec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;

public abstract class EndecRecipeSerializer<R extends Recipe<?>> implements RecipeSerializer<R> {

    private final StructEndec<R> endec;
    private final PacketCodec<PacketByteBuf, R> packetCodec;
    private final MapCodec<R> codec;

    protected EndecRecipeSerializer(StructEndec<R> endec, Endec<R> networkEndec) {
        this.endec = endec;
        this.packetCodec = CodecUtils.toPacketCodec(networkEndec);
        this.codec = CodecUtils.toMapCodec(this.endec, SerializationContext.attributes(SerializationAttributes.HUMAN_READABLE));
    }

    protected EndecRecipeSerializer(StructEndec<R> endec) {
        this(endec, endec);
    }

    @Override
    public MapCodec<R> codec() {
        return this.codec;
    }

    @Override
    public PacketCodec<RegistryByteBuf, R> packetCodec() {
        return this.packetCodec.cast();
    }
}
