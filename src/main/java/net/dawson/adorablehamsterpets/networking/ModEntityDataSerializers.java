package net.dawson.adorablehamsterpets.networking;

import net.dawson.adorablehamsterpets.AdorableHamsterPets;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;

public final class ModEntityDataSerializers {
    public static final EntityDataSerializer<CompoundTag> COMPOUND_TAG = register("compound_tag", new EntityDataSerializer<>() {
        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, CompoundTag> codec() {
            return ByteBufCodecs.COMPOUND_TAG;
        }

        @Override
        public CompoundTag copy(CompoundTag value) {
            return value.copy();
        }
    });

    private ModEntityDataSerializers() {
    }

    private static <T> EntityDataSerializer<T> register(String path, EntityDataSerializer<T> serializer) {
        FabricEntityDataRegistry.register(Identifier.fromNamespaceAndPath(AdorableHamsterPets.MOD_ID, path), serializer);
        return serializer;
    }
}
