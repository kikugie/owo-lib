package io.wispforest.owo.serialization.impl.bytebuf;

import io.netty.buffer.ByteBuf;
import io.wispforest.owo.serialization.MapSerializer;
import io.wispforest.owo.serialization.SequenceSerializer;
import io.wispforest.owo.serialization.Serializer;
import io.wispforest.owo.serialization.StructSerializer;
import io.wispforest.owo.serialization.Codeck;
import io.wispforest.owo.serialization.impl.SerializationAttribute;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encoding.StringEncoding;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.network.encoding.VarLongs;

import java.util.Optional;
import java.util.Set;

public class ByteBufSerializer<T extends ByteBuf> implements Serializer<T> {

    private final T buf;

    public ByteBufSerializer(T buf){
        this.buf = (T) buf;
    }

    public static ByteBufSerializer<PacketByteBuf> packet(){
        return new ByteBufSerializer<>(PacketByteBufs.create());
    }

    //--

    @Override
    public Set<SerializationAttribute> attributes() {
        return Set.of(SerializationAttribute.BINARY);
    }

    //--

    @Override
    public <V> void writeOptional(Codeck<V> codeck, Optional<V> optional) {
        buf.writeBoolean(optional.isPresent());

        optional.ifPresent(v -> codeck.encode(this, v));
    }

    @Override
    public void writeBoolean(boolean value) {
        this.buf.writeBoolean(value);
    }

    @Override
    public void writeByte(byte value) {
        this.buf.writeByte(value);
    }

    @Override
    public void writeShort(short value) {
        this.buf.writeShort(value);
    }

    @Override
    public void writeInt(int value) {
        this.buf.writeInt(value);
    }

    @Override
    public void writeLong(long value) {
        this.buf.writeLong(value);
    }

    @Override
    public void writeFloat(float value) {
        this.buf.writeFloat(value);
    }

    @Override
    public void writeDouble(double value) {
        this.buf.writeDouble(value);
    }

    @Override
    public void writeString(String value) {
        StringEncoding.encode(buf, value, PacketByteBuf.DEFAULT_MAX_STRING_LENGTH);
    }

    @Override
    public void writeBytes(byte[] bytes) {
        writeVarInt(bytes.length);
        this.buf.writeBytes(bytes);
    }

    @Override
    public void writeVarInt(int value) {
        VarInts.write(buf, value);
    }

    @Override
    public void writeVarLong(long value) {
        VarLongs.write(buf, value);
    }

    @Override
    public <V> MapSerializer<V> map(Codeck<V> valueCodec, int length) {
        return (MapSerializer<V>) sequence(valueCodec, length);
    }

    @Override
    public <E> SequenceSerializer<E> sequence(Codeck<E> elementCodec, int length) {
        writeVarInt(length);

        return new ByteBufSequenceSerializer<>(elementCodec);
    }

    @Override
    public StructSerializer struct() {
        return new ByteBufSequenceSerializer(null);
    }

    @Override
    public T result() {
        return (T) buf;
    }

    public class ByteBufSequenceSerializer<V> implements SequenceSerializer<V>, StructSerializer, MapSerializer<V> {

        private final Codeck<V> valueCodec;

        public ByteBufSequenceSerializer(Codeck<V> valueCodec) {
            this.valueCodec = valueCodec;
        }

        @Override
        public void element(V element) {
            field("", valueCodec, element);
        }

        @Override
        public void entry(String key, V value) {
            ByteBufSerializer.this.writeString(key);
            field(key, valueCodec, value);
        }

        @Override
        public <F> StructSerializer field(String name, Codeck<F> codeck, F value) {
            codeck.encode(ByteBufSerializer.this, value);

            return this;
        }

        @Override public void end() {}

    }
}
