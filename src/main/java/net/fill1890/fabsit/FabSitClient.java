package net.fill1890.fabsit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class FabSitClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(FabSit.LOADED_CHANNEL, FabSitClient::checkPacketReply);
    }

    // if the server pings us with a FabSit check packet, reply with the same to confirm it's loaded
    public static void checkPacketReply(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
        client.execute(() -> ClientPlayNetworking.send(FabSit.LOADED_CHANNEL, PacketByteBufs.empty()));
    }
}
