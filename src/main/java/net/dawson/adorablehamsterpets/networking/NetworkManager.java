package net.dawson.adorablehamsterpets.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

/** Fabric networking facade used by the existing packet declarations. */
public final class NetworkManager {
    private NetworkManager() {
    }

    public enum Side {
        C2S,
        S2C
    }

    @FunctionalInterface
    public interface Receiver<T extends CustomPacketPayload> {
        void receive(T payload, PacketContext context);
    }

    public interface PacketContext {
        Player getPlayer();

        void queue(Runnable task);
    }

    public static <T extends CustomPacketPayload> void registerS2CPayloadType(
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        PayloadTypeRegistry.clientboundPlay().register(type, codec);
    }

    public static <T extends CustomPacketPayload> void registerReceiver(
            Side side,
            CustomPacketPayload.Type<T> type,
            StreamCodec<? super RegistryFriendlyByteBuf, T> codec,
            Receiver<T> receiver) {
        if (side == Side.C2S) {
            PayloadTypeRegistry.serverboundPlay().register(type, codec);
            ServerPlayNetworking.registerGlobalReceiver(type, (payload, context) -> receiver.receive(payload, new PacketContext() {
                @Override
                public Player getPlayer() {
                    return context.player();
                }

                @Override
                public void queue(Runnable task) {
                    context.server().execute(task);
                }
            }));
        } else {
            PayloadTypeRegistry.clientboundPlay().register(type, codec);
            ClientPlayNetworking.registerGlobalReceiver(type, (payload, context) -> receiver.receive(payload, new PacketContext() {
                @Override
                public Player getPlayer() {
                    return context.player();
                }

                @Override
                public void queue(Runnable task) {
                    context.client().execute(task);
                }
            }));
        }
    }

    public static void sendToServer(CustomPacketPayload payload) {
        ClientPlayNetworking.send(payload);
    }

    public static void sendToPlayer(ServerPlayer player, CustomPacketPayload payload) {
        ServerPlayNetworking.send(player, payload);
    }

    public static void sendToPlayers(Iterable<? extends ServerPlayer> players, CustomPacketPayload payload) {
        players.forEach(player -> ServerPlayNetworking.send(player, payload));
    }
}
